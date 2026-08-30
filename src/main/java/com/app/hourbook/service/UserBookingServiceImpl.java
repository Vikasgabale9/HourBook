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


    // =========================================
    // Get Available Slots
    // =========================================

    @Override
    public List<BookingSlotDto> getAvailableSlots(
            Integer groundId,
            LocalDate bookingDate) {

        Ground ground = getBookableGround(groundId);

        validateBookingDate(bookingDate);

        LocalTime openingTime = ground.getOpeningTime();

        LocalTime closingTime = ground.getClosingTime();

        Integer slotDuration =
                ground.getSlotDurationMinutes();

        if (openingTime == null || closingTime == null) {

            throw new RuntimeException(
                    "Ground operating hours are not configured"
            );
        }

        if (!closingTime.isAfter(openingTime)) {

            throw new RuntimeException(
                    "Closing time must be after opening time"
            );
        }

        if (slotDuration == null || slotDuration <= 0) {

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
                openingTime.toSecondOfDay() / 60;

        int closeMinutes =
                closingTime.toSecondOfDay() / 60;

        int current =
                openMinutes;

        while (current + slotDuration <= closeMinutes) {

            int slotEndMinutes =
                    current + slotDuration;

            LocalTime slotStart =
                    LocalTime.of(
                            current / 60,
                            current % 60
                    );

            LocalTime slotEnd =
                    LocalTime.of(
                            slotEndMinutes / 60,
                            slotEndMinutes % 60
                    );

            boolean available =
                    !bookedStartTimes.contains(slotStart);


            // Disable past slots for today

            if (bookingDate.equals(LocalDate.now())
                    && !slotStart.isAfter(LocalTime.now())) {

                available = false;
            }


            slots.add(
                    new BookingSlotDto(
                            slotStart,
                            slotEnd,
                            available
                    )
            );

            current =
                    slotEndMinutes;
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
    // Create Booking + Razorpay Order (ATOMIC)
    // =========================================
    //
    // Combines booking creation and Razorpay order
    // creation into a SINGLE transaction. If Razorpay
    // order creation throws (network issue, invalid
    // keys, Razorpay downtime, etc.), the entire
    // transaction - including the booking INSERT -
    // is rolled back. This prevents orphaned PENDING
    // bookings that would otherwise silently block
    // the slot for every other user forever.
    //
    // NOTE: this calls the private helper methods
    // directly (not the public createBooking() /
    // createPaymentOrder()) because calling an
    // @Transactional method on "this" from within the
    // same class bypasses Spring's proxy and would NOT
    // actually join/propagate the outer transaction.
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


        // =========================================
        // Slot Duration
        // =========================================

        Integer slotDuration =
                ground.getSlotDurationMinutes();

        if (slotDuration == null || slotDuration <= 0) {

            slotDuration = 60;
        }


        LocalTime endTime =
                startTime.plusMinutes(slotDuration);


        // =========================================
        // Validate Ground Hours
        // =========================================

        if (startTime.isBefore(ground.getOpeningTime())
                || endTime.isAfter(ground.getClosingTime())) {

            throw new RuntimeException(
                    "Selected slot is outside ground operating hours"
            );
        }


        // =========================================
        // Check Slot Availability Again
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
        // Calculate Price
        // =========================================

        Double pricePerHour =
                ground.getPricePerHour();

        if (pricePerHour == null) {

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

        booking.setBookingDate(bookingDate);

        booking.setStartTime(startTime);

        booking.setEndTime(endTime);

        booking.setPricePerHour(pricePerHour);

        booking.setTotalAmount(totalAmount);

        // Booking is waiting for payment

        booking.setStatus(
                BookingStatus.PENDING
        );


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

        Booking booking =
                bookingRepository.findById(bookingId)
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

        return attachPaymentOrder(booking);
    }


    private Order attachPaymentOrder(Booking booking) {

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
        // Prevent Duplicate Razorpay Order
        // =========================================

        if (booking.getRazorpayOrderId() != null
                && !booking.getRazorpayOrderId().isBlank()) {

            throw new RuntimeException(
                    "Payment order already exists for this booking"
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


        String razorpayOrderId =
                razorpayOrder.get("id");


        // Save Razorpay Order ID

        booking.setRazorpayOrderId(
                razorpayOrderId
        );

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

        Booking booking =
                bookingRepository.findById(bookingId)
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
        // Verify Razorpay Order
        // =========================================

        if (booking.getRazorpayOrderId() == null
                || !booking.getRazorpayOrderId()
                        .equals(razorpayOrderId)) {

            throw new RuntimeException(
                    "Invalid Razorpay order"
            );
        }


        // =========================================
        // Check Already Paid
        // =========================================

        if (booking.getRazorpayPaymentId() != null
                && !booking.getRazorpayPaymentId().isBlank()) {

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

            throw new RuntimeException(
                    "Payment verification failed"
            );
        }


        // =========================================
        // Save Payment Details
        // =========================================

        booking.setRazorpayPaymentId(
                razorpayPaymentId
        );

        booking.setRazorpaySignature(
                razorpaySignature
        );


        // Payment successful

        booking.setStatus(
                BookingStatus.CONFIRMED
        );


        bookingRepository.save(booking);


        return true;
    }


    // =========================================
    // Get User Bookings
    // =========================================

    @Override
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
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Booking not found"
                                )
                        );


        // User can cancel only own booking

        if (!booking.getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to cancel this booking"
            );
        }


        // Already cancelled

        if (booking.getStatus()
                == BookingStatus.CANCELLED) {

            throw new RuntimeException(
                    "This booking is already cancelled"
            );
        }


        // Completed booking cannot be cancelled

        if (booking.getStatus()
                == BookingStatus.COMPLETED) {

            throw new RuntimeException(
                    "Completed bookings cannot be cancelled"
            );
        }


        // Cancel Booking

        booking.setStatus(
                BookingStatus.CANCELLED
        );

        bookingRepository.save(booking);
    }


    // =========================================
    // Get Bookable Ground
    // =========================================

    private Ground getBookableGround(
            Integer groundId) {

        return groundRepository
                .findByIdAndStatusAndIsActiveTrue(
                        groundId,
                        GroundStatus.APPROVED
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ground is not available for booking"
                        )
                );
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
                LocalDate.now()
        )) {

            throw new RuntimeException(
                    "Past dates cannot be booked"
            );
        }
    }

}