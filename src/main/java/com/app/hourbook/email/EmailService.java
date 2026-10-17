package com.app.hourbook.email;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class EmailService {

    private static final String TOKEN_URL =
            "https://oauth2.googleapis.com/token";

    private static final String GMAIL_SEND_URL =
            "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String fromEmail;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailService(
            @Value("${GMAIL_CLIENT_ID}") String clientId,
            @Value("${GMAIL_CLIENT_SECRET}") String clientSecret,
            @Value("${GMAIL_REFRESH_TOKEN}") String refreshToken,
            @Value("${GMAIL_SENDER}") String fromEmail) {

        this.clientId = requireValue(clientId, "GMAIL_CLIENT_ID");
        this.clientSecret = requireValue(
                clientSecret, "GMAIL_CLIENT_SECRET");
        this.refreshToken = requireValue(
                refreshToken, "GMAIL_REFRESH_TOKEN");
        this.fromEmail = requireValue(fromEmail, "GMAIL_SENDER");

        this.restClient = RestClient.builder().build();
    }

    private String requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name);
        }
        return value;
    }

    public String sendOtpEmail(String to, String otp) throws Exception {

        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email is required");
        }

        if (otp == null || !otp.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "OTP must contain exactly six digits");
        }

        String accessToken = getAccessToken();

        MimeMessage message = createOtpMessage(to, otp);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        message.writeTo(output);

        String rawMessage = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(output.toByteArray());

        Map<?, ?> response = restClient.post()
                .uri(GMAIL_SEND_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers ->
                        headers.setBearerAuth(accessToken))
                .body(Map.of("raw", rawMessage))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new IllegalStateException(
                    "Gmail API did not confirm email submission");
        }

        // Preserve the existing method's return contract.
        // Never log or expose the OTP in production.
        return otp;
    }

    private String getAccessToken() {

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        Map<?, ?> response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(
                        MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException(
                    "Google did not return an access token");
        }

        return response.get("access_token").toString();
    }

    private MimeMessage createOtpMessage(
            String to, String otp) throws Exception {

        Properties properties = new Properties();
        Session session = Session.getInstance(properties);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to, true));
        message.setSubject(
                "Your HourBook verification code",
                StandardCharsets.UTF_8.name());

        message.setContent(buildOtpHtml(otp),
                "text/html; charset=UTF-8");

        message.saveChanges();
        return message;
    }

    private String buildOtpHtml(String otp) {

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport"
                    content="width=device-width, initial-scale=1.0">
              <title>HourBook Email Verification</title>
            </head>
            <body style="margin:0;padding:0;
                         background:#f3f6fc;
                         font-family:Arial,Helvetica,sans-serif;
                         color:#182338;">

              <table role="presentation" width="100%%"
                     cellspacing="0" cellpadding="0"
                     style="background:#f3f6fc;padding:32px 12px;">
                <tr>
                  <td align="center">

                    <table role="presentation" width="100%%"
                           cellspacing="0" cellpadding="0"
                           style="max-width:520px;background:#ffffff;
                                  border-radius:18px;overflow:hidden;
                                  box-shadow:0 8px 30px
                                  rgba(22,42,80,0.08);">

                      <tr>
                        <td style="padding:30px 24px;text-align:center;
                                   background:#153eaa;
                                   background-image:linear-gradient(
                                     135deg,#153eaa,#4779f5);">
                          <div style="font-size:30px;font-weight:800;
                                      letter-spacing:-1px;color:#ffffff;">
                            HourBook
                          </div>
                          <div style="margin-top:8px;font-size:13px;
                                      color:#e1eaff;">
                            Your game. Your ground. Your time.
                          </div>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:34px 28px 16px;">
                          <div style="font-size:12px;font-weight:700;
                                      color:#3564d4;letter-spacing:2px;
                                      text-transform:uppercase;">
                            Email verification
                          </div>

                          <h1 style="margin:12px 0;font-size:26px;
                                     line-height:1.3;color:#182338;">
                            Welcome to HourBook!
                          </h1>

                          <p style="margin:0;font-size:15px;
                                    line-height:1.8;color:#5d687b;">
                            You're one step away from getting started.
                            Enter this verification code to confirm
                            your email address.
                          </p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:12px 28px 22px;">
                          <div style="padding:24px 12px;
                                      text-align:center;
                                      background:#f0f5ff;
                                      border:1px solid #dce7ff;
                                      border-radius:14px;">
                            <div style="font-size:11px;font-weight:700;
                                        color:#5b6b87;
                                        letter-spacing:2px;">
                              YOUR ONE-TIME PASSWORD
                            </div>

                            <div style="margin-top:14px;
                                        font-size:34px;font-weight:800;
                                        letter-spacing:8px;
                                        color:#2457d6;">
                              %s
                            </div>

                            <div style="margin-top:12px;
                                        font-size:12px;color:#65738a;">
                              Expires in 5 minutes
                            </div>
                          </div>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:0 28px 28px;">
                          <p style="margin:0;font-size:13px;
                                    line-height:1.8;color:#65738a;">
                            For your security, never share this code
                            with anyone. HourBook will never ask you
                            to disclose your OTP over a phone call.
                          </p>

                          <p style="margin:20px 0 0;font-size:13px;
                                    line-height:1.8;color:#65738a;">
                            Didn't request this code? You can safely
                            ignore this email.
                          </p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:20px 24px;text-align:center;
                                   background:#f8faff;
                                   border-top:1px solid #edf1f8;">
                          <div style="font-size:12px;font-weight:700;
                                      color:#2457d6;">
                            HourBook
                          </div>
                          <div style="margin-top:6px;font-size:11px;
                                      line-height:1.7;color:#8490a4;">
                            Book sports grounds by the hour.<br>
                            This is an automated verification email.
                          </div>
                        </td>
                      </tr>

                    </table>

                  </td>
                </tr>
              </table>

            </body>
            </html>
            """.formatted(otp);
    }

    public String genrateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}