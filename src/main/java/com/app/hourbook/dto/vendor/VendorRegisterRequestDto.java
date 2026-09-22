package com.app.hourbook.dto.vendor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendorRegisterRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(
        min = 2,
        max = 100,
        message = "Full name must be between 2 and 100 characters"
    )
    @Pattern(
        regexp = "^[A-Za-z]+(?:[\\s'-][A-Za-z]+)*$",
        message = "Full name can contain only letters, spaces, hyphens and apostrophes"
    )
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^[6-9]\\d{9}$",
        message = "Enter a valid 10-digit Indian mobile number"
    )
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#()_+=\\-])[A-Za-z\\d@$!%*?&^#()_+=\\-]{8,20}$",
        message = "Password must be 8-20 characters and contain uppercase, lowercase, number and special character"
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}