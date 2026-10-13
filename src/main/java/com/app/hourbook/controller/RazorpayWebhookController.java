package com.app.hourbook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.hourbook.service.RazorpayWebhookService;
import com.app.hourbook.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/webhooks/razorpay")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final PaymentService paymentService;
    private final RazorpayWebhookService webhookService;

    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        if (!paymentService.verifyWebhookSignature(rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid Razorpay webhook signature");
        }

        webhookService.processEvent(rawBody);
        return ResponseEntity.ok("Webhook processed");
    }
}
