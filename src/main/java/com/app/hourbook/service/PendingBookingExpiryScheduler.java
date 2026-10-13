package com.app.hourbook.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.enums.PaymentStatus;
import com.app.hourbook.model.Booking;
import com.app.hourbook.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PendingBookingExpiryScheduler {

    private final BookingRepository bookingRepository;

    @Value("${hourbook.booking.pending-timeout-minutes:15}")
    private long pendingTimeoutMinutes;

    @Scheduled(fixedDelayString = "${hourbook.booking.expiry-check-ms:60000}")
    @Transactional
    public void expireStalePendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(pendingTimeoutMinutes);

        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndPaymentStatusInAndCreatedAtBeforeAndRazorpayOrderIdIsNotNull(
                        BookingStatus.PENDING,
                        List.of(PaymentStatus.CREATED, PaymentStatus.PENDING),
                        cutoff
                );

        for (Booking booking : expiredBookings) {
            // Recheck before changing state; a webhook may have completed payment.
            if (booking.getStatus() == BookingStatus.PENDING
                    && (booking.getPaymentStatus() == PaymentStatus.CREATED
                        || booking.getPaymentStatus() == PaymentStatus.PENDING)) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setPaymentStatus(PaymentStatus.EXPIRED);
            }
        }

        if (!expiredBookings.isEmpty()) {
            bookingRepository.saveAll(expiredBookings);
        }
    }
}
