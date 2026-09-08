package com.app.hourbook.service;

import java.util.List;

import com.app.hourbook.model.Booking;

public interface VendorRevenueService {

    List<Booking> getVendorRevenueBookings(Integer vendorId);

    Double getTotalRevenue(Integer vendorId);
}