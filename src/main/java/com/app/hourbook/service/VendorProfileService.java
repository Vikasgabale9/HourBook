
package com.app.hourbook.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.VendorProfileResponseDto;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorProfileService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private final VendorDetailsRepository repository;
    private final CloudinaryImageService cloudinaryImageService;

    // ================= GET VENDOR PROFILE =================

    @Transactional(readOnly = true)
    public VendorProfileResponseDto getProfile(Integer vendorId) {

        VendorDetails vendor = findVendor(vendorId);

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
                                : vendor.getKycStatus().name()
                )
                .build();
    }

    // ================= GET VENDOR DETAILS =================

    @Transactional(readOnly = true)
    public VendorDetails getVendorDetails(Integer vendorId) {

        return findVendor(vendorId);
    }

    // ================= UPDATE VENDOR PROFILE =================

    @Transactional
    public VendorDetails updateProfile(
            Integer vendorId,
            String fullName,
            String email,
            MultipartFile profileImage) throws IOException {

        VendorDetails vendor = findVendor(vendorId);

        vendor.setFullName(fullName);
        vendor.setEmail(email);

        // Upload only when a new image is selected.
        if (profileImage != null && !profileImage.isEmpty()) {

            validateProfileImage(profileImage);

            String imageUrl = cloudinaryImageService.uploadImage(
                    profileImage,
                    "hourbook/vendors/profiles"
            );

            vendor.setProfileImage(imageUrl);
        }

        return repository.save(vendor);
    }

    // ================= FIND VENDOR =================

    private VendorDetails findVendor(Integer vendorId) {

        return repository.findByVendorAuth_Id(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vendor profile not found"
                        )
                );
    }

    // ================= VALIDATE IMAGE =================

    private void validateProfileImage(MultipartFile image) {

        if (image.getSize() > MAX_IMAGE_SIZE) {

            throw new IllegalArgumentException(
                    "Profile image must not exceed 5 MB."
            );
        }

        String contentType = image.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {

            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are allowed."
            );
        }
    }
}
