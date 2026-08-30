package com.app.hourbook.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;


    // =========================================
    // Create Razorpay Order
    // =========================================

    @Override
    public Order createRazorpayOrder(
            Double amount,
            String receipt) {

        try {

            if (amount == null || amount <= 0) {

                throw new RuntimeException(
                        "Invalid payment amount"
                );

            }

            RazorpayClient razorpayClient =
                    new RazorpayClient(
                            razorpayKeyId,
                            razorpayKeySecret
                    );

            // Razorpay requires JSONObject
            JSONObject options = new JSONObject();

            // Razorpay amount is in paise
            long amountInPaise =
                    Math.round(amount * 100);

            options.put(
                    "amount",
                    amountInPaise
            );

            options.put(
                    "currency",
                    "INR"
            );

            options.put(
                    "receipt",
                    receipt
            );

            return razorpayClient
                    .orders
                    .create(options);

        } catch (Exception e) {

        	e.printStackTrace();
            throw new RuntimeException(
                    "Failed to create Razorpay order",
                    e
            );

        }

    }


    // =========================================
    // Verify Razorpay Payment Signature
    // =========================================

    @Override
    public boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        try {

            if (razorpayOrderId == null
                    || razorpayPaymentId == null
                    || razorpaySignature == null) {

                return false;

            }

            String data =
                    razorpayOrderId
                            + "|"
                            + razorpayPaymentId;

            Mac sha256Hmac =
                    Mac.getInstance(
                            "HmacSHA256"
                    );

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            razorpayKeySecret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    );

            sha256Hmac.init(
                    secretKey
            );

            byte[] hash =
                    sha256Hmac.doFinal(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder calculatedSignature =
                    new StringBuilder();

            for (byte b : hash) {

                calculatedSignature.append(
                        String.format(
                                "%02x",
                                b
                        )
                );

            }

            return calculatedSignature
                    .toString()
                    .equals(
                            razorpaySignature
                    );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to verify Razorpay payment",
                    e
            );

        }

    }

}