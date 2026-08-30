package com.app.hourbook.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Get all bookings for a specific ground on a specific date
    List<Booking> findByGround_IdAndBookingDate(
            Integer groundId,
            LocalDate bookingDate
    );

    // Get bookings that block slots
    List<Booking> findByGround_IdAndBookingDateAndStatusIn(
            Integer groundId,
            LocalDate bookingDate,
            List<BookingStatus> statuses
    );

    // Check whether an exact slot is already booked
    boolean existsByGround_IdAndBookingDateAndStartTimeAndStatusIn(
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime,
            List<BookingStatus> statuses
    );

    // Get all bookings of a user
    List<Booking> findByUser_IdOrderByBookingDateDescStartTimeDesc(
            Integer userId
    );

    // Get bookings of a user by status
    List<Booking> findByUser_IdAndStatusOrderByBookingDateDescStartTimeDesc(
            Integer userId,
            BookingStatus status
    );
    
 // =========================================
 // Get all bookings for a vendor
 // =========================================

 List<Booking> findByGround_Vendor_IdOrderByBookingDateDescStartTimeDesc(
         Integer vendorId
 );


 // =========================================
 // Get bookings for a vendor by status
 // =========================================

 List<Booking> findByGround_Vendor_IdAndStatusOrderByBookingDateDescStartTimeDesc(
         Integer vendorId,
         BookingStatus status
 );
}