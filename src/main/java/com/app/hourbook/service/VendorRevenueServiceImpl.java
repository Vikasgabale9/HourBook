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
public class VendorRevenueServiceImpl implements VendorRevenueService {

    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> getVendorRevenueBookings(Integer vendorId) {

        return bookingRepository
                .findByGround_Vendor_IdAndStatusOrderByBookingDateDescStartTimeDesc(
                        vendorId,
                        BookingStatus.CONFIRMED
                );
    }

    @Override
    public Double getTotalRevenue(Integer vendorId) {

        List<Booking> bookings =
                getVendorRevenueBookings(vendorId);

        return bookings.stream()
                .map(Booking::getTotalAmount)
                .filter(amount -> amount != null)
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}