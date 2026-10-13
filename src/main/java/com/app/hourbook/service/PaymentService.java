package com.app.hourbook.service;

import com.razorpay.Order;

public interface PaymentService {

    Order createRazorpayOrder(
            Double amount,
            String receipt
    );

    boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    );

    /** Verify Razorpay webhook signature using the webhook secret
     * configured in the Razorpay Dashboard (not the API key secret).
     */
    boolean verifyWebhookSignature(String rawBody, String signature);
}