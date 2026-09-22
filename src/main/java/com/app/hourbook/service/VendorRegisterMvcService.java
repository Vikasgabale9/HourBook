package com.app.hourbook.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.hourbook.dto.vendor.VendorRegisterRequestDto;
import com.app.hourbook.email.EmailService;
import com.app.hourbook.enums.KycStatus;
import com.app.hourbook.enums.Role;
import com.app.hourbook.model.Vendor;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;
import com.app.hourbook.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorRegisterMvcService {

    private final VendorRepository vendorRepository;
    private final VendorDetailsRepository vendorDetailsRepository;
    private final EmailService emailService;

    // =========================================================
    // REGISTER VENDOR
    // =========================================================

    @Transactional
    public Integer register(
            VendorRegisterRequestDto request) throws Exception {

        // -----------------------------------------------------
        // Password confirmation
        // -----------------------------------------------------

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException(
                    "Password and confirm password do not match"
            );
        }

        // -----------------------------------------------------
        // Email already exists
        // -----------------------------------------------------

        if (vendorDetailsRepository
                .existsByEmail(request.getEmail())) {

            throw new RuntimeException(
                    "Email is already registered"
            );
        }

        // -----------------------------------------------------
        // Check existing Vendor auth record
        // -----------------------------------------------------

        Vendor vendor =
                vendorRepository
                        .findByPhoneNumber(
                                request.getPhoneNumber()
                        )
                        .orElse(null);

        // -----------------------------------------------------
        // Already verified vendor
        // -----------------------------------------------------

        if (vendor != null && vendor.isVerified()) {

            throw new RuntimeException(
                    "Phone number is already registered"
            );
        }

        // -----------------------------------------------------
        // Existing unverified Vendor
        // OR create new Vendor
        // -----------------------------------------------------

        if (vendor == null) {

            vendor = new Vendor();

            vendor.setPhoneNumber(
                    request.getPhoneNumber()
            );

            vendor.setRole(
                    Role.ROLE_VENDOR
            );
        }

        vendor.setPhoneNumber(
                request.getPhoneNumber()
        );

        vendor.setRole(
                Role.ROLE_VENDOR
        );

        vendor.setVerified(false);

        // REST token is NOT generated here.
        vendor.setToken(null);

        // -----------------------------------------------------
        // Generate EMAIL OTP
        // -----------------------------------------------------

        String otp =
                emailService.genrateOtp();

        vendor.setOtp(otp);

        vendor =
                vendorRepository.save(vendor);

        // -----------------------------------------------------
        // Existing profile
        // -----------------------------------------------------

        VendorDetails details =
                vendorDetailsRepository
                        .findByVendorAuthId(
                                vendor.getId()
                        )
                        .orElse(null);

        if (details == null) {

            details = new VendorDetails();

            details.setVendorAuth(vendor);
        }

        details.setFullName(
                request.getFullName()
        );

        details.setPhoneNumber(
                request.getPhoneNumber()
        );

        details.setEmail(
                request.getEmail()
        );

        details.setPassword(
                request.getPassword()
        );

        details.setOtp(otp);

        details.setEmailVerified(false);

        details.setActive(true);

        if (details.getKycStatus() == null) {

            details.setKycStatus(
                    KycStatus.NOT_SUBMITTED
            );
        }

        vendorDetailsRepository.save(details);

        // -----------------------------------------------------
        // Send OTP
        // -----------------------------------------------------

        emailService.sendOtpEmail(
                request.getEmail(),
                otp
        );

        return vendor.getId();
    }

    // =========================================================
    // VERIFY REGISTRATION OTP
    // =========================================================

    @Transactional
    public VendorDetails verifyOtp(
            Integer vendorId,
            String otp) {

        Vendor vendor =
                vendorRepository
                        .findById(vendorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vendor not found"
                                )
                        );

        VendorDetails details =
                vendorDetailsRepository
                        .findByVendorAuthId(vendorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vendor profile not found"
                                )
                        );

        // -----------------------------------------------------
        // OTP check
        // -----------------------------------------------------

        if (vendor.getOtp() == null) {

            throw new RuntimeException(
                    "OTP expired or already used"
            );
        }

        if (!vendor.getOtp().equals(otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        // -----------------------------------------------------
        // Mark verified
        // -----------------------------------------------------

        vendor.setOtp(null);

        vendor.setVerified(true);

        vendorRepository.save(vendor);

        details.setOtp(null);

        details.setEmailVerified(true);

        vendorDetailsRepository.save(details);

        return details;
    }
}