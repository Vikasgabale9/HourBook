package com.app.hourbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProfileResponseDto {

    private Integer vendorId;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String profileImage;

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private Boolean active;

    private String kycStatus;

}