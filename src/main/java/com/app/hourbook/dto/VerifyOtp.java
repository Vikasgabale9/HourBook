package com.app.hourbook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record VerifyOtp(

        @NotBlank(message = "email number is required")
        @Email
        String email,

        @NotBlank(message = "OTP is required")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "OTP must be 6 digits"
        )
        String otp

) {
}