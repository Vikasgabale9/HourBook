package com.app.hourbook.service;

import java.util.List;

import com.app.hourbook.model.Booking;

public interface VendorBookingService {

    List<Booking> getVendorBookings(Integer vendorId);

    List<Booking> getVendorBookingsByStatus(
            Integer vendorId,
            String status
    );
}