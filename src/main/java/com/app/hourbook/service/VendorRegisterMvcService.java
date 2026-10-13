
package com.app.hourbook.service;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // REGISTER VENDOR
    // =========================================================

    @Transactional
    public Integer register(VendorRegisterRequestDto request)
            throws Exception {

        // Validate password confirmation
        if (request.getPassword() == null
                || request.getConfirmPassword() == null
                || !request.getPassword()
                        .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Password and confirm password do not match"
            );
        }

        // Check whether email is already registered
        if (vendorDetailsRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        // Find existing vendor authentication record
        Vendor vendor = vendorRepository
                .findByPhoneNumber(request.getPhoneNumber())
                .orElse(null);

        // Reject an already verified vendor
        if (vendor != null && vendor.isVerified()) {
            throw new IllegalArgumentException(
                    "Phone number is already registered"
            );
        }

        // Create a new vendor if no record exists
        if (vendor == null) {
            vendor = new Vendor();
            vendor.setPhoneNumber(request.getPhoneNumber());
        }

        vendor.setPhoneNumber(request.getPhoneNumber());
        vendor.setRole(Role.ROLE_VENDOR);
        vendor.setVerified(false);

        // Do not generate a REST token during MVC registration
        vendor.setToken(null);

        // Generate email OTP
        String otp = emailService.genrateOtp();
        vendor.setOtp(otp);

        vendor = vendorRepository.save(vendor);

        // Find an existing profile or create one
        VendorDetails details = vendorDetailsRepository
                .findByVendorAuthId(vendor.getId())
                .orElse(null);

        if (details == null) {
            details = new VendorDetails();
            details.setVendorAuth(vendor);
        }

        details.setFullName(request.getFullName());
        details.setPhoneNumber(request.getPhoneNumber());
        details.setEmail(request.getEmail());

        // Hash the password before storing it
        details.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        details.setOtp(otp);
        details.setEmailVerified(false);
        details.setActive(true);

        if (details.getKycStatus() == null) {
            details.setKycStatus(KycStatus.NOT_SUBMITTED);
        }

        vendorDetailsRepository.save(details);

        // Send OTP to the registered email
        emailService.sendOtpEmail(request.getEmail(), otp);

        return vendor.getId();
    }

    // =========================================================
    // VERIFY REGISTRATION OTP
    // =========================================================

    @Transactional
    public VendorDetails verifyOtp(Integer vendorId, String otp) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Vendor not found")
                );

        VendorDetails details = vendorDetailsRepository
                .findByVendorAuthId(vendorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vendor profile not found"
                        )
                );

        // Validate OTP
        if (vendor.getOtp() == null) {
            throw new IllegalArgumentException(
                    "OTP expired or already used"
            );
        }

        if (otp == null || !vendor.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Mark vendor authentication as verified
        vendor.setOtp(null);
        vendor.setVerified(true);
        vendorRepository.save(vendor);

        // Mark vendor email as verified
        details.setOtp(null);
        details.setEmailVerified(true);
        vendorDetailsRepository.save(details);

        return details;
    }
}
