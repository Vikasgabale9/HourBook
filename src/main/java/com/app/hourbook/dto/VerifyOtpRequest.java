package com.app.hourbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(\\+91)?[6-9]\\d{9}$",
                message = "Phone number must be a valid Indian mobile number"
        )
        String phoneNumber,

        @NotBlank(message = "OTP is required")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "OTP must be 6 digits"
        )
        String otp

) {
}