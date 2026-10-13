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
public class PaymentServiceImpl
        implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret:}")
    private String razorpayWebhookSecret;


    // =========================================
    // Create Razorpay Order
    // =========================================

    @Override
    public Order createRazorpayOrder(
            Double amount,
            String receipt) {

        try {

            // =========================================
            // Validate Amount
            // =========================================

            if (amount == null
                    || amount <= 0) {

                throw new RuntimeException(
                        "Invalid payment amount"
                );
            }

            if (razorpayKeyId == null
                    || razorpayKeyId.isBlank()) {

                throw new RuntimeException(
                        "Razorpay Key ID is not configured"
                );
            }

            if (razorpayKeySecret == null
                    || razorpayKeySecret.isBlank()) {

                throw new RuntimeException(
                        "Razorpay Key Secret is not configured"
                );
            }

            // =========================================
            // Razorpay Client
            // =========================================

            RazorpayClient razorpayClient =
                    new RazorpayClient(
                            razorpayKeyId,
                            razorpayKeySecret
                    );

            // =========================================
            // Amount in Paise
            // =========================================

            long amountInPaise =
                    Math.round(
                            amount * 100
                    );

            if (amountInPaise <= 0) {

                throw new RuntimeException(
                        "Invalid Razorpay amount"
                );
            }

            // =========================================
            // Razorpay Order Options
            // =========================================

            JSONObject options =
                    new JSONObject();

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

            // =========================================
            // Create Order
            // =========================================

            Order order =
                    razorpayClient
                            .orders
                            .create(options);

            if (order == null) {

                throw new RuntimeException(
                        "Razorpay returned an empty order"
                );
            }

            String orderId =
                    order.get("id");

            if (orderId == null
                    || orderId.isBlank()) {

                throw new RuntimeException(
                        "Razorpay order ID was not returned"
                );
            }

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "RAZORPAY ORDER CREATED"
            );

            System.out.println(
                    "Order ID: " + orderId
            );

            System.out.println(
                    "Amount: "
                            + amountInPaise
                            + " paise"
            );

            System.out.println(
                    "========================================"
            );

            return order;

        } catch (RuntimeException e) {

            throw e;

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

            // =========================================
            // Validate Input
            // =========================================

            if (razorpayOrderId == null
                    || razorpayOrderId.isBlank()) {

                return false;
            }

            if (razorpayPaymentId == null
                    || razorpayPaymentId.isBlank()) {

                return false;
            }

            if (razorpaySignature == null
                    || razorpaySignature.isBlank()) {

                return false;
            }

            if (razorpayKeySecret == null
                    || razorpayKeySecret.isBlank()) {

                throw new RuntimeException(
                        "Razorpay Key Secret is not configured"
                );
            }

            // =========================================
            // Razorpay Signature Data
            //
            // order_id + "|" + payment_id
            // =========================================

            String data =
                    razorpayOrderId
                            + "|"
                            + razorpayPaymentId;

            // =========================================
            // HMAC SHA-256
            // =========================================

            Mac sha256Hmac =
                    Mac.getInstance(
                            "HmacSHA256"
                    );

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            razorpayKeySecret
                                    .getBytes(
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

            // =========================================
            // Convert Hash to Hex
            // =========================================

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

            String expectedSignature =
                    calculatedSignature.toString();

            boolean verified =
                    expectedSignature.equals(
                            razorpaySignature
                    );

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "RAZORPAY SIGNATURE VERIFICATION"
            );

            System.out.println(
                    "Order ID: "
                            + razorpayOrderId
            );

            System.out.println(
                    "Payment ID: "
                            + razorpayPaymentId
            );

            System.out.println(
                    "Signature Valid: "
                            + verified
            );

            System.out.println(
                    "========================================"
            );

            return verified;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to verify Razorpay payment",
                    e
            );
        }
    }


    // =========================================
    // Verify Razorpay Webhook Signature
    // =========================================

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        try {
            if (rawBody == null || signature == null || signature.isBlank()
                    || razorpayWebhookSecret == null || razorpayWebhookSecret.isBlank()) {
                return false;
            }

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    razorpayWebhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(key);
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }

            return java.security.MessageDigest.isEqual(
                    hex.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            return false;
        }
    }
}