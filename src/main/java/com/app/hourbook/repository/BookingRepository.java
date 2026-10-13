package com.app.hourbook.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByGround_IdAndBookingDate(
            Integer groundId,
            LocalDate bookingDate
    );

    List<Booking> findByGround_IdAndBookingDateAndStatusIn(
            Integer groundId,
            LocalDate bookingDate,
            List<BookingStatus> statuses
    );

    boolean existsByGround_IdAndBookingDateAndStartTimeAndStatusIn(
            Integer groundId,
            LocalDate bookingDate,
            LocalTime startTime,
            List<BookingStatus> statuses
    );

    /*
     * Detect overlapping bookings.
     *
     * Existing booking overlaps the requested interval when:
     * existingStart < requestedEnd
     * AND existingEnd > requestedStart
     */
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.ground.id = :groundId
          AND b.bookingDate = :bookingDate
          AND b.status IN :statuses
          AND b.startTime < :requestedEnd
          AND b.endTime > :requestedStart
        """)
    boolean existsOverlappingBooking(
            @Param("groundId") Integer groundId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("requestedStart") LocalTime requestedStart,
            @Param("requestedEnd") LocalTime requestedEnd,
            @Param("statuses") List<BookingStatus> statuses
    );

    List<Booking> findByUser_IdOrderByBookingDateDescStartTimeDesc(
            Integer userId
    );

    List<Booking> findByUser_IdAndStatusOrderByBookingDateDescStartTimeDesc(
            Integer userId,
            BookingStatus status
    );

    List<Booking> findByGround_Vendor_IdOrderByBookingDateDescStartTimeDesc(
            Integer vendorId
    );

    List<Booking> findByGround_Vendor_IdAndStatusOrderByBookingDateDescStartTimeDesc(
            Integer vendorId,
            BookingStatus status
    );

    long countByUser_Id(Integer userId);

    long countByUser_IdAndStatus(
            Integer userId,
            BookingStatus status
    );

    long countByUser_IdAndBookingDateGreaterThanEqualAndStatus(
            Integer userId,
            LocalDate bookingDate,
            BookingStatus status
    );

    long countByGround_Vendor_Id(Integer vendorId);

    // Pending orders older than the reservation timeout are released by a scheduler.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Booking> findByStatusAndPaymentStatusInAndCreatedAtBeforeAndRazorpayOrderIdIsNotNull(
            BookingStatus status,
            List<com.app.hourbook.enums.PaymentStatus> paymentStatuses,
            LocalDateTime cutoff
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);
}