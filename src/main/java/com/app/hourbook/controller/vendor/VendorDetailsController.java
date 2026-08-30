package com.app.hourbook.controller.vendor;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.VendorDetailsRequestDto;
import com.app.hourbook.dto.VendorDetailsResponseDto;
import com.app.hourbook.dto.VendorKycRequestDto;
import com.app.hourbook.dto.VendorKycResponseDto;
import com.app.hourbook.dto.VerifyOtp;
import com.app.hourbook.security.AuthenticatedPrincipal;
import com.app.hourbook.service.VendorDetailsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendorDetails")
@Validated
public class VendorDetailsController {

    private final VendorDetailsService vendorDetailsService;

    public VendorDetailsController(VendorDetailsService vendorDetailsService) {
        this.vendorDetailsService = vendorDetailsService;
    }

    // =====================================================
    // Vendor Profile Registration
    // =====================================================

 
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> createProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody VendorDetailsRequestDto request)
            throws Exception {

        return ResponseEntity.ok(
                vendorDetailsService.createProfile(
                        principal.getId(),
                        request));
    }

    // =====================================================
    // Verify Email OTP
    // =====================================================

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VendorDetailsResponseDto>> verifyOtp(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody VerifyOtp request) {

        return ResponseEntity.ok(
                vendorDetailsService.verifyOtp(
                        request,
                        principal.getId()));
    }

    // =====================================================
    // Submit Vendor KYC
    // =====================================================

    @PostMapping(value = "/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VendorKycResponseDto>> submitKyc(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
           @Valid @ModelAttribute VendorKycRequestDto request)
            throws IOException {

        VendorKycResponseDto response =
                vendorDetailsService.submitKyc(
                        principal.getId(),
                        request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vendor KYC submitted successfully.",
                        HttpStatus.OK.value(),
                        response));
    }

    // =====================================================
    // Get Vendor KYC
    // =====================================================

    @GetMapping("/kyc")
    public ResponseEntity<ApiResponse<VendorKycResponseDto>> getKyc(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {

        VendorKycResponseDto response =
                vendorDetailsService.getKyc(
                        principal.getId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vendor KYC fetched successfully.",
                        HttpStatus.OK.value(),
                        response));
    }

    // =====================================================
    // Update Vendor KYC
    // =====================================================

    @PutMapping(value = "/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VendorKycResponseDto>> updateKyc(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
          @Valid  @ModelAttribute VendorKycRequestDto request)
            throws IOException {

        VendorKycResponseDto response =
                vendorDetailsService.updateKyc(
                        principal.getId(),
                        request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vendor KYC updated successfully.",
                        HttpStatus.OK.value(),
                        response));
    }
    
}