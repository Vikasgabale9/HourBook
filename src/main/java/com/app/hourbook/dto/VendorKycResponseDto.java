package com.app.hourbook.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorKycResponseDto {

    private Integer vendorId;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Boolean emailVerified;

    private String aadhaarNumber;

    private String aadhaarImage;

    private String panNumber;

    private String panImage;

    private LocalDate dateOfBirth;

    private String profileImage;

    private String kycStatus;

}