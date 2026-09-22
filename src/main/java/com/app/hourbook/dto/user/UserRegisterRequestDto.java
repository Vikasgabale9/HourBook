package com.app.hourbook.dto.user;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequestDto {

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
    @Size(
        min = 8,
        max = 20,
        message = "Password must be between 8 and 20 characters"
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    private MultipartFile profileImage;
}