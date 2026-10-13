package com.app.hourbook.service;

import java.util.Optional;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.enums.PaymentStatus;
import com.app.hourbook.model.Booking;
import com.app.hourbook.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RazorpayWebhookService {

    private final BookingRepository bookingRepository;

    @Transactional
    public void processEvent(String rawBody) {
        JSONObject root = new JSONObject(rawBody);
        String event = root.optString("event", "");

        JSONObject payload = root.optJSONObject("payload");
        JSONObject paymentEntity = null;
        String orderId = "";
        String paymentId = "";

        if (payload != null && payload.optJSONObject("payment") != null) {
            paymentEntity = payload.getJSONObject("payment").optJSONObject("entity");
        }

        if ("order.paid".equals(event)) {
            JSONObject orderEntity = payload == null || payload.optJSONObject("order") == null
                    ? null
                    : payload.getJSONObject("order").optJSONObject("entity");
            if (orderEntity != null) {
                orderId = orderEntity.optString("id", "");
            }
        }

        if (paymentEntity != null) {
            if (orderId.isBlank()) {
                orderId = paymentEntity.optString("order_id", "");
            }
            paymentId = paymentEntity.optString("id", "");
        }

        if (orderId.isBlank()) {
            return;
        }

        Optional<Booking> optionalBooking = bookingRepository.findByRazorpayOrderId(orderId);
        if (optionalBooking.isEmpty()) {
            // The order may not belong to this application; acknowledge to avoid endless retries.
            return;
        }

        Booking booking = optionalBooking.get();

        switch (event) {
            case "payment.captured", "order.paid" -> markPaid(booking, paymentId);
            case "payment.failed" -> markFailed(booking, paymentId);
            case "payment.authorized" -> {
                if (booking.getStatus() == BookingStatus.PENDING
                        && booking.getPaymentStatus() != PaymentStatus.PAID) {
                    booking.setPaymentStatus(PaymentStatus.PENDING);
                    bookingRepository.save(booking);
                }
            }
            default -> {
                // Ignore other event types. Pending reservations are released by timeout scheduler.
            }
        }
    }

    private void markPaid(Booking booking, String paymentId) {
        if (paymentId != null && !paymentId.isBlank()) {
            booking.setRazorpayPaymentId(paymentId);
        }

        // A late payment must not re-open an already expired/cancelled slot.
        // Record the payment state so it can be reconciled/refunded manually if needed.
        booking.setPaymentStatus(PaymentStatus.PAID);
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        bookingRepository.save(booking);
    }

    private void markFailed(Booking booking, String paymentId) {
        if (booking.getPaymentStatus() == PaymentStatus.PAID
                || booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        if (paymentId != null && !paymentId.isBlank()) {
            booking.setRazorpayPaymentId(paymentId);
        }
        booking.setPaymentStatus(PaymentStatus.FAILED);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}
