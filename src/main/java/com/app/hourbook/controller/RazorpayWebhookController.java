
package com.app.hourbook.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.hourbook.service.PaymentService;
import com.app.hourbook.service.RazorpayWebhookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/webhooks/razorpay")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final PaymentService paymentService;
    private final RazorpayWebhookService webhookService;
    
    private static final Logger log =
            LoggerFactory.getLogger(RazorpayWebhookController.class);

    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String rawBody,
            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false
            ) String signature,
            @RequestHeader(
                    value = "x-razorpay-event-id",
                    required = false
            ) String eventId) {

        // Verify the original, unmodified request body first.
        if (!paymentService.verifyWebhookSignature(rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid Razorpay webhook signature");
        }

        if (eventId == null || eventId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing Razorpay event ID");
        }

        // Exceptions should propagate so Spring can return a 5xx response.
        // Razorpay can then retry transient processing failures.
        webhookService.processEvent(rawBody, eventId);
 
        log.info("Razorpay webhook received successfully. Event ID: {}", eventId);
      
        return ResponseEntity.ok("Webhook received");
    }
}
