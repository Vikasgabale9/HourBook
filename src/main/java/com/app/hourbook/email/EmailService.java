
package com.app.hourbook.email;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

@Service
public class EmailService {

    private final Resend resend;
    private final String fromEmail;

    public EmailService(
            @Value("${RESEND_API_KEY}") String apiKey,
            @Value("${RESEND_FROM:onboarding@resend.dev}") String fromEmail) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY environment variable is missing");
        }

        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    public String sendOtpEmail(String to, String otp) throws Exception {

        String subject = "HourBook - OTP Verification";

        String body = """
                <div style="font-family:Arial,sans-serif;max-width:500px;
                            margin:auto;padding:24px;color:#172033">
                    <h2 style="color:#245eea">Welcome to HourBook!</h2>
                    <p>Use the verification code below to verify your email.</p>
                    <div style="font-size:30px;font-weight:bold;
                                letter-spacing:8px;padding:20px;
                                background:#f1f5ff;text-align:center;
                                border-radius:8px">
                        %s
                    </div>
                    <p>This OTP is valid for 5 minutes.</p>
                    <p>If you did not request this code, you can ignore this email.</p>
                </div>
                """.formatted(otp);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(body)
                .build();

        CreateEmailResponse response = resend.emails().send(params);

        if (response == null || response.getId() == null) {
            throw new IllegalStateException(
                    "Resend did not confirm email submission");
        }

        // Do not log OTPs or recipient email addresses.
        return otp;
    }

    public String genrateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
