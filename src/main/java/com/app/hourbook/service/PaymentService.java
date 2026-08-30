package com.app.hourbook.service;

import com.razorpay.Order;

public interface PaymentService {

    // =========================================
    // Create Razorpay Order
    // =========================================

    Order createRazorpayOrder(
            Double amount,
            String receipt
    );

    // =========================================
    // Verify Razorpay Payment Signature
    // =========================================

    boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    );
}