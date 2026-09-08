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
}