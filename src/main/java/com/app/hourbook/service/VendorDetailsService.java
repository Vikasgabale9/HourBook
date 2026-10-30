
package com.app.hourbook.service;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.VendorDetailsRequestDto;
import com.app.hourbook.dto.VendorDetailsResponseDto;
import com.app.hourbook.dto.VendorKycRequestDto;
import com.app.hourbook.dto.VendorKycResponseDto;
import com.app.hourbook.dto.VerifyOtp;
import com.app.hourbook.email.EmailService;
import com.app.hourbook.enums.KycStatus;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.exception.VendorNotFoundException;
import com.app.hourbook.model.Vendor;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;
import com.app.hourbook.repository.VendorRepository;
import com.app.hourbook.security.JwtService;

@Service
public class VendorDetailsService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorDetailsRepository vendorDetailsRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CloudinaryImageService cloudinaryImageService;

    // =====================================================
    // IMAGE VALIDATION CONFIGURATION
    // =====================================================

    private static final long MAX_KYC_FILE_SIZE =
            5L * 1024 * 1024;

    private static final Set<String> ALLOWED_KYC_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    // =====================================================
    // GET VENDOR BY PHONE NUMBER
    // =====================================================

    public VendorDetails getVendorByPhoneNumber(String phoneNumber) {

        return vendorDetailsRepository
                .findByVendorAuthPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new VendorNotFoundException(
                                "Vendor not found with: " + phoneNumber
                        )
                );
    }

    // =====================================================
    // CREATE VENDOR PROFILE
    // =====================================================

    public ApiResponse<String> createProfile(
            Integer vendorId,
            VendorDetailsRequestDto request) throws Exception {

        Vendor vendor = vendorRepository
                .findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found")
                );

        if (!vendor.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new RuntimeException(
                    "PhoneNumber not matching with Token"
            );
        }

        String otp = emailService.genrateOtp();

        VendorDetails details = new VendorDetails();

        details.setEmail(request.getEmail());
        details.setFullName(request.getFullName());
        details.setPassword(request.getPassword());
        details.setPhoneNumber(request.getPhoneNumber());
        details.setOtp(otp);
        details.setVendorAuth(vendor);

        vendorDetailsRepository.save(details);

        emailService.sendOtpEmail(request.getEmail(), otp);

        return ApiResponse.success(
                "Vendor profile created successfully",
                201,
                "Verify otp shared on Email"
        );
    }

    // =====================================================
    // VERIFY EMAIL OTP
    // =====================================================

    public ApiResponse<VendorDetailsResponseDto> verifyOtp(
            VerifyOtp request,
            Integer id) {

        VendorDetails vendor = vendorDetailsRepository
                .findByVendorAuthId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found")
                );

        if (!request.email().equals(vendor.getEmail())) {
            throw new RuntimeException(
                    "Token details are not matching with VendorDetails."
            );
        }

        if (!request.otp().equals(vendor.getOtp())) {
            throw new RuntimeException("Invalid otp");
        }

        vendor.setEmailVerified(true);
        vendor.setOtp(null);

        vendorDetailsRepository.save(vendor);

        VendorDetailsResponseDto response =
                mapToResponseDto(vendor);

        return ApiResponse.success(
                "Email verified successfully.",
                201,
                response
        );
    }

    // =====================================================
    // SUBMIT NEW KYC
    // =====================================================


public VendorKycResponseDto submitKyc(
        Integer vendorId,
        VendorKycRequestDto dto) throws IOException {

    VendorDetails vendor = vendorDetailsRepository
            .findByVendorAuthId(vendorId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Vendor not found"));

    if (!vendor.getPhoneNumber().equals(dto.getPhoneNumber())) {
        throw new InputMismatchException(
                "Phone number does not match this vendor.");
    }

    validateKycImage(dto.getAadhaarImage(), "Aadhaar");
    validateKycImage(dto.getPanImage(), "PAN");

    // Upload documents as authenticated Cloudinary assets.
    String aadhaarPublicId =
            cloudinaryImageService.uploadKycDocument(
                    dto.getAadhaarImage(), "AADHAAR");

    String panPublicId =
            cloudinaryImageService.uploadKycDocument(
                    dto.getPanImage(), "PAN");

    // Update vendor details.
    vendor.setFullName(dto.getFullName());
    vendor.setAadhaarNumber(dto.getAadhaarNumber());
    vendor.setPanNumber(dto.getPanNumber());
    vendor.setDateOfBirth(dto.getDateOfBirth());

    // Store Cloudinary public IDs, not public URLs.
    vendor.setAadhaarImage(aadhaarPublicId);
    vendor.setPanImage(panPublicId);

    // Optional profile image remains a normal public image.
    if (dto.getProfileImage() != null &&
            !dto.getProfileImage().isEmpty()) {

        String profileUrl = cloudinaryImageService.uploadImage(
                dto.getProfileImage(),
                "hourbook/vendors/profiles");

        vendor.setProfileImage(profileUrl);
    }

    vendor.setKycStatus(KycStatus.PENDING);

    VendorDetails savedVendor =
            vendorDetailsRepository.save(vendor);

    return mapToKycResponse(savedVendor);
}

    // =====================================================
    // GET VENDOR KYC
    // =====================================================

    public VendorKycResponseDto getKyc(Integer vendorId) {

        VendorDetails vendor = vendorDetailsRepository
                .findByVendorAuthId(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found")
                );

        return mapToKycResponse(vendor);
    }

    // =====================================================
    // UPDATE EXISTING KYC
    // =====================================================


public VendorKycResponseDto updateKyc(
        Integer vendorId,
        VendorKycRequestDto dto) throws IOException {

    VendorDetails vendor = vendorDetailsRepository
            .findByVendorAuthId(vendorId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Vendor not found"));

    if (!vendor.getPhoneNumber().equals(dto.getPhoneNumber())) {
        throw new InputMismatchException(
                "Phone number does not match this vendor.");
    }

    vendor.setFullName(dto.getFullName());
    vendor.setAadhaarNumber(dto.getAadhaarNumber());
    vendor.setPanNumber(dto.getPanNumber());
    vendor.setDateOfBirth(dto.getDateOfBirth());

    // Replace Aadhaar only if a new file is supplied.
    if (dto.getAadhaarImage() != null &&
            !dto.getAadhaarImage().isEmpty()) {

        String aadhaarPublicId =
                cloudinaryImageService.uploadKycDocument(
                        dto.getAadhaarImage(), "AADHAAR");

        vendor.setAadhaarImage(aadhaarPublicId);
    }

    // Replace PAN only if a new file is supplied.
    if (dto.getPanImage() != null &&
            !dto.getPanImage().isEmpty()) {

        String panPublicId =
                cloudinaryImageService.uploadKycDocument(
                        dto.getPanImage(), "PAN");

        vendor.setPanImage(panPublicId);
    }

    // Profile image remains publicly deliverable.
    if (dto.getProfileImage() != null &&
            !dto.getProfileImage().isEmpty()) {

        String profileUrl = cloudinaryImageService.uploadImage(
                dto.getProfileImage(),
                "hourbook/vendors/profiles");

        vendor.setProfileImage(profileUrl);
    }

    vendor.setKycStatus(KycStatus.PENDING);

    VendorDetails savedVendor =
            vendorDetailsRepository.save(vendor);

    return mapToKycResponse(savedVendor);
}


    // =====================================================
    // MAP VENDOR DETAILS RESPONSE DTO
    // =====================================================

    private VendorDetailsResponseDto mapToResponseDto(
            VendorDetails vendorDetails) {

        VendorDetailsResponseDto dto =
                new VendorDetailsResponseDto();

        dto.setEmail(vendorDetails.getEmail());
        dto.setFullName(vendorDetails.getFullName());
        dto.setId(vendorDetails.getId());

        if (vendorDetails.getVendorAuth() != null) {

            dto.setAuthId(
                    vendorDetails.getVendorAuth().getId()
            );

            dto.setPhoneNumber(
                    vendorDetails.getVendorAuth().getPhoneNumber()
            );
        }

        return dto;
    }

    // =====================================================
    // MAP KYC RESPONSE DTO
    // =====================================================


private VendorKycResponseDto mapToKycResponse(VendorDetails vendor) {

    return VendorKycResponseDto.builder()

            .vendorId(
                    vendor.getVendorAuth().getId()
            )

            .fullName(
                    vendor.getFullName()
            )

            .email(
                    vendor.getEmail()
            )

            .phoneNumber(
                    vendor.getPhoneNumber()
            )

            .emailVerified(
                    vendor.isEmailVerified()
            )

            .aadhaarNumber(
                    vendor.getAadhaarNumber()
            )

            // These fields now contain Cloudinary public IDs,
            // not publicly accessible image URLs.
            .aadhaarImage(
                    vendor.getAadhaarImage()
            )

            .panNumber(
                    vendor.getPanNumber()
            )

            .panImage(
                    vendor.getPanImage()
            )

            .dateOfBirth(
                    vendor.getDateOfBirth()
            )

            // Profile image remains a normal public image URL.
            .profileImage(
                    vendor.getProfileImage()
            )

            .kycStatus(
                    vendor.getKycStatus() == null
                            ? "Not Submitted"
                            : vendor.getKycStatus().name()
            )

            .build();
}

    // =====================================================
    // VALIDATE KYC IMAGE
    // =====================================================

    private void validateKycImage(
            MultipartFile image,
            String documentName) {

        if (image == null || image.isEmpty()) {

            throw new IllegalArgumentException(
                    documentName + " image is required."
            );
        }

        if (image.getSize() > MAX_KYC_FILE_SIZE) {

            throw new IllegalArgumentException(
                    documentName + " image must not exceed 5 MB."
            );
        }

        String contentType = image.getContentType();

        if (contentType == null ||
                !ALLOWED_KYC_TYPES.contains(contentType)) {

            throw new IllegalArgumentException(
                    documentName + " must be JPG, PNG or WEBP."
            );
        }
    }
}
