
package com.app.hourbook.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.hourbook.dto.booking.BookingSlotDto;
import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.enums.PaymentStatus;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.model.Booking;
import com.app.hourbook.model.Ground;
import com.app.hourbook.repository.AppUserDetailsRepository;
import com.app.hourbook.repository.BookingRepository;
import com.app.hourbook.repository.GroundRepository;
import com.razorpay.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBookingServiceImpl implements UserBookingService {

    private final GroundRepository groundRepository;
    private final BookingRepository bookingRepository;
    private final AppUserDetailsRepository userRepository;
    private final PaymentService paymentService;

    private static final List<BookingStatus> BLOCKING_STATUSES =
            List.of(
                    BookingStatus.PENDING,
                    BookingStatus.CONFIRMED
            );

    // =========================================
    // Get Available Slots
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<BookingSlotDto> getAvailableSlots(
            Integer groundId,
            LocalDate bookingDate) {

        Ground ground = getBookableGround(groundId);

        validateBookingDate(bookingDate);

        LocalTime openingTime = ground.getOpeningTime();
        LocalTime closingTime = ground.getClosingTime();

        Integer configuredDuration = ground.getSlotDurationMinutes();

        if (openingTime == null || closingTime == null) {
            throw new IllegalArgumentException(
                    "Ground operating hours are not configured"
            );
        }

        if (!closingTime.isAfter(openingTime)) {
            throw new IllegalArgumentException(
                    "Closing time must be after opening time"
            );
        }

        int slotDuration =
                configuredDuration == null || configuredDuration <= 0
                        ? 60
                        : configuredDuration;

        long totalMinutes = java.time.Duration.between(
                openingTime,
                closingTime
        ).toMinutes();

        if (slotDuration > totalMinutes) {
            throw new IllegalArgumentException(
                    "Slot duration (" + slotDuration
                    + " minutes) exceeds the ground's operating window ("
                    + totalMinutes
                    + " minutes). Update the ground's slot duration."
            );
        }

        List<Booking> existingBookings =
                bookingRepository
                        .findByGround_IdAndBookingDateAndStatusIn(
                                groundId,
                                bookingDate,
                                BLOCKING_STATUSES
                        );

        List<BookingSlotDto> slots = new ArrayList<>();

        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        // The loop is bounded by the ground's operating hours.
        for (long elapsed = 0;
             elapsed + slotDuration <= totalMinutes;
             elapsed += slotDuration) {

            LocalTime slotStart = openingTime.plusMinutes(elapsed);
            LocalTime slotEnd = slotStart.plusMinutes(slotDuration);

            boolean booked = existingBookings.stream()
                    .anyMatch(existing ->
                            existing.getStartTime() != null
                            && existing.getEndTime() != null
                            && slotStart.isBefore(existing.getEndTime())
                            && slotEnd.isAfter(existing.getStartTime())
                    );

            boolean pastSlot =
                    bookingDate.equals(today)
                    && !slotStart.isAfter(now);

            BookingSlotDto slot = new BookingSlotDto();
            slot.setStartTime(slotStart);
            slot.setEndTime(slotEnd);
            slot.setAvailable(!booked && !pastSlot);

            slots.add(slot);
        }

        return slots;
    } 	

    // =========================================
    // Create Booking
    // =========================================

    @Override
    @Transactional
    public Booking createBooking(
            Integer userId,
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime) {

        return buildAndSaveBooking(
                userId,
                groundId,
                bookingDate,
                startTime
        );
    }

    // =========================================
    // Create Booking + Razorpay Order
    // =========================================

    @Override
    @Transactional
    public Booking createBookingAndPaymentOrder(
            Integer userId,
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime) {

        Booking booking = buildAndSaveBooking(
                userId,
                groundId,
                bookingDate,
                startTime
        );

        attachPaymentOrder(booking);

        return booking;
    }

    // =========================================
    // Build and Save Booking
    // =========================================

    private Booking buildAndSaveBooking(
            Integer userId,
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime) {

        validateBookingDate(bookingDate);

        if (startTime == null) {
            throw new IllegalArgumentException(
                    "Booking start time is required"
            );
        }

        /*
         * Acquire a database lock before checking availability.
         * Concurrent booking requests for this ground must wait
         * until the current booking transaction finishes.
         */
        Ground ground = getBookableGroundForUpdate(groundId);

        AppUserDetails user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        LocalTime openingTime = ground.getOpeningTime();
        LocalTime closingTime = ground.getClosingTime();

        if (openingTime == null || closingTime == null) {
            throw new IllegalArgumentException(
                    "Ground operating hours are not configured"
            );
        }

        Integer configuredDuration = ground.getSlotDurationMinutes();

        int slotDuration =
                configuredDuration == null || configuredDuration <= 0
                        ? 60
                        : configuredDuration;

        LocalTime endTime = startTime.plusMinutes(slotDuration);

        // =========================================
        // Validate Ground Hours
        // =========================================

        if (startTime.isBefore(openingTime)
                || endTime.isAfter(closingTime)) {

            throw new IllegalArgumentException(
                    "Selected slot is outside ground operating hours"
            );
        }

        // =========================================
        // Validate Slot Alignment
        // =========================================

        long minutesFromOpening =
                java.time.Duration.between(
                        openingTime,
                        startTime
                ).toMinutes();

        if (minutesFromOpening < 0
                || minutesFromOpening % slotDuration != 0) {

            throw new IllegalArgumentException(
                    "Selected start time does not match the ground's slot schedule"
            );
        }

        // =========================================
        // Check Overlapping Bookings
        // =========================================

        boolean overlapping =
                bookingRepository.existsOverlappingBooking(
                        groundId,
                        bookingDate,
                        startTime,
                        endTime,
                        BLOCKING_STATUSES
                );

        if (overlapping) {
            throw new IllegalArgumentException(
                    "This time slot is already booked. Please select another slot."
            );
        }

        // =========================================
        // Validate Price
        // =========================================

        Double pricePerHour = ground.getPricePerHour();

        if (pricePerHour == null || pricePerHour <= 0) {
            throw new IllegalArgumentException(
                    "Ground price is not configured"
            );
        }

        double totalAmount =
                pricePerHour * slotDuration / 60.0;

        // =========================================
        // Create Booking
        // =========================================

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setGround(ground);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPricePerHour(pricePerHour);
        booking.setTotalAmount(totalAmount);

        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.CREATED);

        return bookingRepository.save(booking);
    }

    // =========================================
    // Create Razorpay Payment Order
    // =========================================

    @Override
    @Transactional
    public Order createPaymentOrder(
            Integer bookingId,
            Integer userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found")
                );

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized for this booking"
            );
        }

        return attachPaymentOrder(booking);
    }

    // =========================================
    // Attach Razorpay Order
    // =========================================

    private Order attachPaymentOrder(Booking booking) {

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancelled booking cannot be paid"
            );
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Booking is already confirmed"
            );
        }

        if (booking.getRazorpayOrderId() != null
                && !booking.getRazorpayOrderId().isBlank()) {

            throw new IllegalStateException(
                    "Payment order already exists for this booking. "
                    + "Please continue with the existing payment."
            );
        }

        String receipt = "booking_" + booking.getId();

        Order razorpayOrder = paymentService.createRazorpayOrder(
                booking.getTotalAmount(),
                receipt
        );

        if (razorpayOrder == null) {
            throw new IllegalStateException(
                    "Razorpay did not return an order"
            );
        }

        String razorpayOrderId = razorpayOrder.get("id");

        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            throw new IllegalStateException(
                    "Invalid Razorpay order ID"
            );
        }

        booking.setRazorpayOrderId(razorpayOrderId);
        booking.setPaymentStatus(PaymentStatus.CREATED);

        bookingRepository.save(booking);

        return razorpayOrder;
    }

    // =========================================
    // Verify Razorpay Payment
    // =========================================

    @Override
    @Transactional
    public boolean verifyPayment(
            Integer bookingId,
            Integer userId,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        if (razorpayOrderId == null || razorpayOrderId.isBlank()
                || razorpayPaymentId == null || razorpayPaymentId.isBlank()
                || razorpaySignature == null || razorpaySignature.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay payment details are required"
            );
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found")
                );

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized for this booking"
            );
        }

        if (booking.getRazorpayOrderId() == null
                || !booking.getRazorpayOrderId().equals(razorpayOrderId)) {

            throw new IllegalArgumentException(
                    "Invalid Razorpay order"
            );
        }

        if (booking.getRazorpayPaymentId() != null
                && !booking.getRazorpayPaymentId().isBlank()) {

            throw new IllegalStateException(
                    "Payment already completed"
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending bookings can be confirmed by payment"
            );
        }

        boolean verified = paymentService.verifyPayment(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature
        );

        if (!verified) {
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);

            /*
             * Do not throw here after saving FAILED in the same
             * transaction: a RuntimeException would roll back that
             * status update. Return false so the update can commit.
             */
            return false;
        }

        booking.setRazorpayPaymentId(razorpayPaymentId);
        booking.setRazorpaySignature(razorpaySignature);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        return true;
    }

    // =========================================
    // Get User Bookings
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Integer userId) {

        return bookingRepository
                .findByUser_IdOrderByBookingDateDescStartTimeDesc(
                        userId
                );
    }

    // =========================================
    // Cancel Booking
    // =========================================

    @Override
    @Transactional
    public void cancelBooking(
            Integer bookingId,
            Integer userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found")
                );

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized for this booking"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking is already cancelled"
            );
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Completed booking cannot be cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // =========================================
    // Get Bookable Ground
    // =========================================

    private Ground getBookableGround(Integer groundId) {

        Ground ground = groundRepository.findById(groundId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ground not found")
                );

        validateGroundBookable(ground);

        return ground;
    }

    private Ground getBookableGroundForUpdate(Integer groundId) {

        Ground ground = groundRepository.findByIdForUpdate(groundId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ground not found")
                );

        validateGroundBookable(ground);

        return ground;
    }

    private void validateGroundBookable(Ground ground) {

        if (ground.getStatus() != GroundStatus.APPROVED) {
            throw new IllegalStateException(
                    "This ground is not approved"
            );
        }

        if (!Boolean.TRUE.equals(ground.getIsActive())) {
            throw new IllegalStateException(
                    "This ground is currently inactive"
            );
        }
    }

    // =========================================
    // Validate Booking Date
    // =========================================

    private void validateBookingDate(LocalDate bookingDate) {

        if (bookingDate == null) {
            throw new IllegalArgumentException(
                    "Booking date is required"
            );
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Cannot book a past date"
            );
        }
    }
}
