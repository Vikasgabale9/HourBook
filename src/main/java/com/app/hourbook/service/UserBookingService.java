package com.app.hourbook.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.app.hourbook.dto.booking.BookingSlotDto;
import com.app.hourbook.model.Booking;
import com.razorpay.Order;

public interface UserBookingService {

    List<BookingSlotDto> getAvailableSlots(
            Integer groundId,
            LocalDate bookingDate
    );

    Booking createBooking(
            Integer userId,
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime
    );

    /*
     * Atomic version: creates the PENDING booking AND the
     * Razorpay order in a single transaction. If Razorpay order
     * creation fails (network issue, invalid keys, Razorpay
     * downtime, etc.), the whole transaction rolls back - so no
     * orphaned PENDING booking is left behind blocking the slot.
     *
     * Prefer this over calling createBooking() +
     * createPaymentOrder() separately.
     */
    Booking createBookingAndPaymentOrder(
            Integer userId,
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime
    );

    // Create Razorpay payment order
    Order createPaymentOrder(
            Integer bookingId,
            Integer userId
    );

    // Verify payment and confirm booking
    boolean verifyPayment(
            Integer bookingId,
            Integer userId,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    );

    List<Booking> getUserBookings(
            Integer userId
    );

    void cancelBooking(
            Integer bookingId,
            Integer userId
    );

}