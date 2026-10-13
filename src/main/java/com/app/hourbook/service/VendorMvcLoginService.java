package com.app.hourbook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.VendorDashLoginReq;
import com.app.hourbook.exception.InvalidCredentialsException;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorMvcLoginService {

    private final VendorDetailsRepository vendorDetailsRepository;
    
    private final PasswordEncoder passwordEncoder;	

    public VendorDetails login(
            VendorDashLoginReq request) {

        VendorDetails vendor =
                vendorDetailsRepository
                        .findByPhoneNumberOrEmail(
                                request.getLoginId(),
                                request.getLoginId()
                        )
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid login credentials"
                                )
                        );

        // =====================================================
        // ONLY PASSWORD CHECK
        // =====================================================

        if (!passwordEncoder.matches(
                request.getPassword(),
                vendor.getPassword())) {

            throw new InvalidCredentialsException(
                "Invalid login credentials"
            );
        }

        return vendor;
    }
}