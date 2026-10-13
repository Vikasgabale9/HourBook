package com.app.hourbook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.VendorDashLoginReq;
import com.app.hourbook.exception.InvalidCredentialsException;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class VendorLoginService {

    @Autowired
    private VendorDetailsRepository vendorDetailsRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public VendorDetails login(VendorDashLoginReq req) {

        VendorDetails vendor = vendorDetailsRepository
                .findByPhoneNumberOrEmail(
                        req.getLoginId(),
                        req.getLoginId())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid login credentials"));

        if (!passwordEncoder.matches(
                req.getPassword(),
                vendor.getPassword())) {

            throw new InvalidCredentialsException(
                "Invalid login credentials"
            );
        }

        if (!vendor.getVendorAuth().isVerified()) {
            throw new InvalidCredentialsException(
                    "Phone number is not verified");
        }

        if (!vendor.isEmailVerified()) {
            throw new InvalidCredentialsException(
                    "Email is not verified");
        }

        if (!vendor.isActive()) {
            throw new InvalidCredentialsException(
                    "Vendor account is inactive");
        }

        return vendor;
    }

}