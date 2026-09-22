package com.app.hourbook.dto.vendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VendorRegisterOtpDto {

    @NotBlank(message = "OTP is required")
    @Pattern(
        regexp = "^\\d{6}$",
        message = "OTP must be 6 digits"
    )
    private String otp;
}