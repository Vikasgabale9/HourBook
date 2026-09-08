package com.app.hourbook.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
public class UserBookingServiceImpl
        implements UserBookingService {

    private final GroundRepository groundRepository;

    private final BookingRepository bookingRepository;

    private final AppUserDetailsRepository userRepository;

    private final PaymentService paymentService;


    // =========================================
    // Get Available Slots
    // =========================================

    @Override
    public List<BookingSlotDto> getAvailableSlots(
            Integer groundId,
            LocalDate bookingDate) {

        Ground ground =
                getBookableGround(groundId);

        validateBookingDate(bookingDate);

        LocalTime openingTime =
                ground.getOpeningTime();

        LocalTime closingTime =
                ground.getClosingTime();

        Integer slotDuration =
                ground.getSlotDurationMinutes();

        if (openingTime == null
                || closingTime == null) {

            throw new RuntimeException(
                    "Ground operating hours are not configured"
            );
        }

        if (!closingTime.isAfter(openingTime)) {

            throw new RuntimeException(
                    "Closing time must be after opening time"
            );
        }

        if (slotDuration == null
                || slotDuration <= 0) {

            slotDuration = 60;
        }

        List<BookingStatus> blockingStatuses =
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED
                );

        List<Booking> existingBookings =
                bookingRepository
                        .findByGround_IdAndBookingDateAndStatusIn(
                                groundId,
                                bookingDate,
                                blockingStatuses
                        );

        Set<LocalTime> bookedStartTimes =
                existingBookings.stream()
                        .map(Booking::getStartTime)
                        .collect(Collectors.toSet());

        List<BookingSlotDto> slots =
                new ArrayList<>();

        // =========================================
        // Generate Slots
        // =========================================

        int openMinutes =
                openingTime.getHour() * 60
                        + openingTime.getMinute();

        int closeMinutes =
                closingTime.getHour() * 60
                        + closingTime.getMinute();

        LocalTime currentTime =
                openingTime;

        while (true) {

            int currentMinutes =
                    currentTime.getHour() * 60
                            + currentTime.getMinute();

            int endMinutes =
                    currentMinutes
                            + slotDuration;

            if (endMinutes > closeMinutes) {
                break;
            }

            LocalTime endTime =
                    currentTime.plusMinutes(
                            slotDuration
                    );

            boolean booked =
                    bookedStartTimes.contains(
                            currentTime
                    );

            boolean pastSlot = false;

            if (bookingDate.equals(LocalDate.now())) {

                pastSlot =
                        currentTime.isBefore(
                                LocalTime.now()
                        );
            }

            boolean available =
                    !booked && !pastSlot;

            BookingSlotDto slot =
                    new BookingSlotDto();

            slot.setStartTime(currentTime);
            slot.setEndTime(endTime);
            slot.setAvailable(available);

            slots.add(slot);

            currentTime =
                    currentTime.plusMinutes(
                            slotDuration
                    );
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

        Booking booking =
                buildAndSaveBooking(
                        userId,
                        groundId,
                        bookingDate,
                        startTime
                );

        attachPaymentOrder(booking);

        return booking;
    }


    // =========================================
    // Build Booking
    // =========================================

    private Booking buildAndSaveBooking(
            Integer userId,
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime) {

        validateBookingDate(bookingDate);

        Ground ground =
                getBookableGround(groundId);

        AppUserDetails user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        if (ground.getOpeningTime() == null
                || ground.getClosingTime() == null) {

            throw new RuntimeException(
                    "Ground operating hours are not configured"
            );
        }

        Integer slotDuration =
                ground.getSlotDurationMinutes();

        if (slotDuration == null
                || slotDuration <= 0) {

            slotDuration = 60;
        }

        LocalTime endTime =
                startTime.plusMinutes(
                        slotDuration
                );

        // =========================================
        // Validate Ground Hours
        // =========================================

        if (startTime.isBefore(
                ground.getOpeningTime())
                || endTime.isAfter(
                        ground.getClosingTime())) {

            throw new RuntimeException(
                    "Selected slot is outside ground operating hours"
            );
        }

        // =========================================
        // Check Slot Availability
        // =========================================

        List<BookingStatus> blockingStatuses =
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED
                );

        boolean alreadyBooked =
                bookingRepository
                        .existsByGround_IdAndBookingDateAndStartTimeAndStatusIn(
                                groundId,
                                bookingDate,
                                startTime,
                                blockingStatuses
                        );

        if (alreadyBooked) {

            throw new RuntimeException(
                    "This slot is already booked"
            );
        }

        // =========================================
        // Price
        // =========================================

        Double pricePerHour =
                ground.getPricePerHour();

        if (pricePerHour == null
                || pricePerHour <= 0) {

            throw new RuntimeException(
                    "Ground price is not configured"
            );
        }

        Double totalAmount =
                pricePerHour
                        * slotDuration
                        / 60.0;

        // =========================================
        // Create Booking
        // =========================================

        Booking booking =
                new Booking();

        booking.setUser(user);

        booking.setGround(ground);

        booking.setBookingDate(
                bookingDate
        );

        booking.setStartTime(
                startTime
        );

        booking.setEndTime(
                endTime
        );

        booking.setPricePerHour(
                pricePerHour
        );

        booking.setTotalAmount(
                totalAmount
        );

        // Waiting for payment
        booking.setStatus(
                BookingStatus.PENDING
        );

        // Payment order will be created next
        booking.setPaymentStatus(
                PaymentStatus.CREATED
        );

        return bookingRepository.save(
                booking
        );
    }


    // =========================================
    // Create Razorpay Payment Order
    // =========================================

    @Override
    @Transactional
    public Order createPaymentOrder(
            Integer bookingId,
            Integer userId) {

        Booking booking =
                bookingRepository.findById(
                        bookingId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found"
                        )
                );

        // =========================================
        // Security Check
        // =========================================

        if (!booking.getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized for this booking"
            );
        }

        return attachPaymentOrder(
                booking
        );
    }


    // =========================================
    // Attach Razorpay Order
    // =========================================

    private Order attachPaymentOrder(
            Booking booking) {

        // =========================================
        // Check Booking Status
        // =========================================

        if (booking.getStatus()
                == BookingStatus.CANCELLED) {

            throw new RuntimeException(
                    "Cancelled booking cannot be paid"
            );
        }

        if (booking.getStatus()
                == BookingStatus.CONFIRMED) {

            throw new RuntimeException(
                    "Booking is already paid"
            );
        }

        // =========================================
        // Reuse Existing Razorpay Order
        // =========================================

        if (booking.getRazorpayOrderId() != null
                && !booking.getRazorpayOrderId()
                        .isBlank()) {

            throw new RuntimeException(
                    "Payment order already exists for this booking. Please continue with the existing payment."
            );
        }

        // =========================================
        // Create Razorpay Order
        // =========================================

        String receipt =
                "booking_" + booking.getId();

        Order razorpayOrder =
                paymentService.createRazorpayOrder(
                        booking.getTotalAmount(),
                        receipt
                );

        if (razorpayOrder == null) {

            throw new RuntimeException(
                    "Razorpay did not return an order"
            );
        }

        String razorpayOrderId =
                razorpayOrder.get("id");

        if (razorpayOrderId == null
                || razorpayOrderId.isBlank()) {

            throw new RuntimeException(
                    "Invalid Razorpay order ID"
            );
        }

        booking.setRazorpayOrderId(
                razorpayOrderId
        );

        booking.setPaymentStatus(
                PaymentStatus.CREATED
        );

        bookingRepository.save(
                booking
        );

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

        // =========================================
        // Validate Request
        // =========================================

        if (razorpayOrderId == null
                || razorpayOrderId.isBlank()) {

            throw new RuntimeException(
                    "Razorpay order ID is missing"
            );
        }

        if (razorpayPaymentId == null
                || razorpayPaymentId.isBlank()) {

            throw new RuntimeException(
                    "Razorpay payment ID is missing"
            );
        }

        if (razorpaySignature == null
                || razorpaySignature.isBlank()) {

            throw new RuntimeException(
                    "Razorpay signature is missing"
            );
        }

        // =========================================
        // Find Booking
        // =========================================

        Booking booking =
                bookingRepository.findById(
                        bookingId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found"
                        )
                );

        // =========================================
        // Security Check
        // =========================================

        if (!booking.getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized for this booking"
            );
        }

        // =========================================
        // Verify Order ID
        // =========================================

        if (booking.getRazorpayOrderId() == null
                || !booking.getRazorpayOrderId()
                        .equals(razorpayOrderId)) {

            throw new RuntimeException(
                    "Invalid Razorpay order"
            );
        }

        // =========================================
        // Already Paid Check
        // =========================================

        if (booking.getRazorpayPaymentId() != null
                && !booking.getRazorpayPaymentId()
                        .isBlank()) {

            throw new RuntimeException(
                    "Payment already completed"
            );
        }

        // =========================================
        // Verify Signature
        // =========================================

        boolean verified =
                paymentService.verifyPayment(
                        razorpayOrderId,
                        razorpayPaymentId,
                        razorpaySignature
                );

        if (!verified) {

            booking.setPaymentStatus(
                    PaymentStatus.FAILED
            );

            bookingRepository.save(
                    booking
            );

            throw new RuntimeException(
                    "Payment verification failed"
            );
        }

        // =========================================
        // Save Razorpay Payment Details
        // =========================================

        booking.setRazorpayPaymentId(
                razorpayPaymentId
        );

        booking.setRazorpaySignature(
                razorpaySignature
        );

        // =========================================
        // IMPORTANT FIX
        // =========================================

        booking.setPaymentStatus(
                PaymentStatus.PAID
        );

        booking.setStatus(
                BookingStatus.CONFIRMED
        );

        bookingRepository.save(
                booking
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "PAYMENT VERIFIED SUCCESSFULLY"
        );

        System.out.println(
                "Booking ID: " + booking.getId()
        );

        System.out.println(
                "Order ID: "
                        + booking.getRazorpayOrderId()
        );

        System.out.println(
                "Payment ID: "
                        + booking.getRazorpayPaymentId()
        );

        System.out.println(
                "Payment Status: "
                        + booking.getPaymentStatus()
        );

        System.out.println(
                "Booking Status: "
                        + booking.getStatus()
        );

        System.out.println(
                "========================================"
        );

        return true;
    }


    // =========================================
    // Get User Bookings
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(
            Integer userId) {

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

        Booking booking =
                bookingRepository.findById(
                        bookingId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found"
                        )
                );

        if (!booking.getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized for this booking"
            );
        }

        if (booking.getStatus()
                == BookingStatus.CANCELLED) {

            throw new RuntimeException(
                    "Booking is already cancelled"
            );
        }

        if (booking.getStatus()
                == BookingStatus.COMPLETED) {

            throw new RuntimeException(
                    "Completed booking cannot be cancelled"
            );
        }

        booking.setStatus(
                BookingStatus.CANCELLED
        );

        bookingRepository.save(
                booking
        );
    }


    // =========================================
    // Get Bookable Ground
    // =========================================

    private Ground getBookableGround(
            Integer groundId) {

        Ground ground =
                groundRepository.findById(
                        groundId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ground not found"
                        )
                );

        if (ground.getStatus()
                != GroundStatus.APPROVED) {

            throw new RuntimeException(
                    "This ground is not approved"
            );
        }

        if (!ground.getIsActive()) {

            throw new RuntimeException(
                    "This ground is currently inactive"
            );
        }

        return ground;
    }


    // =========================================
    // Validate Booking Date
    // =========================================

    private void validateBookingDate(
            LocalDate bookingDate) {

        if (bookingDate == null) {

            throw new RuntimeException(
                    "Booking date is required"
            );
        }

        if (bookingDate.isBefore(
                LocalDate.now())) {

            throw new RuntimeException(
                    "Cannot book a past date"
            );
        }
    }
}