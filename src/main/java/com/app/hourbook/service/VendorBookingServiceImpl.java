package com.app.hourbook.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.model.Booking;
import com.app.hourbook.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorBookingServiceImpl implements VendorBookingService {

    private final BookingRepository bookingRepository;

    // =========================================
    // Get All Vendor Bookings
    // =========================================

    @Override
    public List<Booking> getVendorBookings(Integer vendorId) {

        return bookingRepository
                .findVendorBookingsWithUserAndGround(vendorId);
    }

    // =========================================
    // Get Vendor Bookings By Status
    // =========================================

    @Override
    public List<Booking> getVendorBookingsByStatus(
            Integer vendorId,
            String status) {

        // If status is empty, return all vendor bookings.
        if (status == null || status.isBlank()) {
            return getVendorBookings(vendorId);
        }

        BookingStatus bookingStatus;

        try {
            bookingStatus = BookingStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid booking status: " + status,
                    e
            );
        }

        // Fetch bookings with user and ground for the selected status.
        return bookingRepository
                .findVendorBookingsWithUserAndGroundByStatus(
                        vendorId,
                        bookingStatus
                );
    }
}