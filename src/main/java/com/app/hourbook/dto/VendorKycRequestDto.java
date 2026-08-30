package com.app.hourbook.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VendorKycRequestDto {

    @NotBlank
    private String fullName;

    @NotBlank(message = "Phone number is required.")
    @Pattern(
        regexp = "^[6-9]\\d{9}$",
        message = "Phone number must be a valid 10-digit Indian mobile number."
    )
    private String phoneNumber;
    
    @NotBlank
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$")
    private String aadhaarNumber;	

    @NotBlank
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$")
    private String panNumber;

    @NotNull
    private LocalDate dateOfBirth;

    private MultipartFile aadhaarImage;

    private MultipartFile panImage;

    private MultipartFile profileImage;

}