package com.app.hourbook.service;

import org.springframework.stereotype.Service;

import com.app.hourbook.dto.VendorProfileResponseDto;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorProfileService {

    private final VendorDetailsRepository repository;

    public VendorProfileResponseDto getProfile(Integer vendorId){

        VendorDetails vendor = repository
                .findByVendorAuthId(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        return VendorProfileResponseDto.builder()

                .vendorId(vendor.getVendorAuth().getId())

                .fullName(vendor.getFullName())

                .email(vendor.getEmail())

                .phoneNumber(vendor.getPhoneNumber())

                .profileImage(vendor.getProfileImage())

                .emailVerified(vendor.isEmailVerified())

                .phoneVerified(vendor.getVendorAuth().isVerified())

                .active(vendor.isActive())

                .kycStatus(
                        vendor.getKycStatus() == null
                                ? "Not Submitted"
                                : vendor.getKycStatus().name())

                .build();

    }

}