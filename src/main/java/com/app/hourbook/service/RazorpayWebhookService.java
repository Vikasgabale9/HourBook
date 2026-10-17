
package com.app.hourbook.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.enums.PaymentStatus;
import com.app.hourbook.model.Booking;
import com.app.hourbook.model.WebhookEvent;
import com.app.hourbook.repository.BookingRepository;
import com.app.hourbook.repository.WebhookEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RazorpayWebhookService {

    private final BookingRepository bookingRepository;
    private final WebhookEventRepository webhookEventRepository;

    @Transactional
    public void processEvent(String rawBody, String eventId) {

        JSONObject root = new JSONObject(rawBody);
        String event = root.optString("event", "");

        // Atomically register this event. A duplicate is not processed again.
        int inserted = webhookEventRepository.insertIfAbsent(
                eventId,
                event.isBlank() ? "unknown" : event
        );

        if (inserted == 0) {
            return;
        }

        WebhookEvent webhookEvent = webhookEventRepository
                .findByEventId(eventId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Webhook event record was not created"
                        ));

        JSONObject payload = root.optJSONObject("payload");

        JSONObject paymentEntity = null;
        JSONObject orderEntity = null;

        if (payload != null) {
            JSONObject payment = payload.optJSONObject("payment");
            if (payment != null) {
                paymentEntity = payment.optJSONObject("entity");
            }

            JSONObject order = payload.optJSONObject("order");
            if (order != null) {
                orderEntity = order.optJSONObject("entity");
            }
        }

        String orderId = "";
        String paymentId = "";

        if ("order.paid".equals(event) && orderEntity != null) {
            orderId = orderEntity.optString("id", "");
        }

        if (paymentEntity != null) {
            if (orderId.isBlank()) {
                orderId = paymentEntity.optString("order_id", "");
            }

            paymentId = paymentEntity.optString("id", "");
        }

        if (orderId.isBlank()) {
            finishEvent(
                    webhookEvent,
                    "IGNORED",
                    "Webhook did not contain a usable order ID"
            );
            return;
        }

        Optional<Booking> optionalBooking =
                bookingRepository.findByRazorpayOrderId(orderId);

        if (optionalBooking.isEmpty()) {
            finishEvent(
                    webhookEvent,
                    "IGNORED",
                    "No HourBook booking matches this Razorpay order"
            );
            return;
        }

        Booking booking = optionalBooking.get();

        // Unknown events are recorded but do not change the booking.
        if (!event.equals("payment.captured")
                && !event.equals("order.paid")
                && !event.equals("payment.failed")
                && !event.equals("payment.authorized")) {

            finishEvent(webhookEvent, "IGNORED", null);
            return;
        }

        if (paymentEntity == null) {
            finishEvent(
                    webhookEvent,
                    "REVIEW_REQUIRED",
                    "Expected payment entity is missing"
            );
            return;
        }

        // Confirm that the payment belongs to this order and has the
        // expected amount and currency before changing booking state.
        String validationError =
                validatePayment(paymentEntity, orderId, booking);

        if (validationError != null) {
            finishEvent(
                    webhookEvent,
                    "REVIEW_REQUIRED",
                    validationError
            );
            return;
        }

        switch (event) {
            case "payment.captured", "order.paid" -> {
                String paymentStatus =
                        paymentEntity.optString("status", "");

                if (!"captured".equalsIgnoreCase(paymentStatus)) {
                    finishEvent(
                            webhookEvent,
                            "REVIEW_REQUIRED",
                            "Success event did not contain a captured payment"
                    );
                    return;
                }

                boolean needsReconciliation =
                        markPaid(booking, paymentId);

                finishEvent(
                        webhookEvent,
                        needsReconciliation
                                ? "REVIEW_REQUIRED"
                                : "PROCESSED",
                        needsReconciliation
                                ? "Payment captured after booking expiry/cancellation; reconcile or refund"
                                : null
                );
            }

            case "payment.failed" -> {
                String paymentStatus =
                        paymentEntity.optString("status", "");

                if (!"failed".equalsIgnoreCase(paymentStatus)) {
                    finishEvent(
                            webhookEvent,
                            "REVIEW_REQUIRED",
                            "Failure event did not contain a failed payment"
                    );
                    return;
                }

                markFailed(booking, paymentId);
                finishEvent(webhookEvent, "PROCESSED", null);
            }

            case "payment.authorized" -> {
                String paymentStatus =
                        paymentEntity.optString("status", "");

                if (!"authorized".equalsIgnoreCase(paymentStatus)) {
                    finishEvent(
                            webhookEvent,
                            "REVIEW_REQUIRED",
                            "Authorization event status did not match"
                    );
                    return;
                }

                if (booking.getStatus() == BookingStatus.PENDING
                        && booking.getPaymentStatus() != PaymentStatus.PAID) {

                    booking.setPaymentStatus(PaymentStatus.PENDING);
                    bookingRepository.save(booking);
                }

                finishEvent(webhookEvent, "PROCESSED", null);
            }

            default -> finishEvent(webhookEvent, "IGNORED", null);
        }
    }

    private String validatePayment(
            JSONObject payment,
            String orderId,
            Booking booking) {

        String paymentOrderId = payment.optString("order_id", "");

        if (!orderId.equals(paymentOrderId)) {
            return "Payment order ID does not match the booking order";
        }

        String currency = payment.optString("currency", "");

        if (!"INR".equalsIgnoreCase(currency)) {
            return "Unexpected payment currency";
        }

        long receivedAmount = payment.optLong("amount", -1L);

        if (receivedAmount <= 0) {
            return "Payment amount is missing or invalid";
        }

        Double bookingAmount = booking.getTotalAmount();

        if (bookingAmount == null || bookingAmount <= 0) {
            return "Booking amount is missing or invalid";
        }

        long expectedAmountInPaise =
                Math.round(bookingAmount * 100.0);

        if (receivedAmount != expectedAmountInPaise) {
            return "Payment amount does not match the booking amount";
        }

        return null;
    }

    /*
     * Returns true if a captured payment needs manual reconciliation.
     */
    private boolean markPaid(Booking booking, String paymentId) {

        if (paymentId != null && !paymentId.isBlank()) {
            booking.setRazorpayPaymentId(paymentId);
        }

        boolean expired =
                booking.getPaymentStatus() == PaymentStatus.EXPIRED;

        boolean cancelled =
                booking.getStatus() == BookingStatus.CANCELLED;

        booking.setPaymentStatus(PaymentStatus.PAID);

        if (expired || cancelled) {
            // Never reopen a cancelled or expired reservation automatically.
            bookingRepository.save(booking);
            return true;
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }

        bookingRepository.save(booking);
        return false;
    }

    private void markFailed(Booking booking, String paymentId) {

        // A late failure must not reverse a successful payment.
        if (booking.getPaymentStatus() == PaymentStatus.PAID
                || booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            return;
        }

        if (paymentId != null && !paymentId.isBlank()) {
            booking.setRazorpayPaymentId(paymentId);
        }

        booking.setPaymentStatus(PaymentStatus.FAILED);

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CANCELLED);
        }

        bookingRepository.save(booking);
    }

    private void finishEvent(
            WebhookEvent event,
            String status,
            String errorMessage) {

        event.setStatus(status);
        event.setProcessedAt(LocalDateTime.now());
        event.setErrorMessage(errorMessage);

        webhookEventRepository.save(event);
    }
}
