package com.app.hourbook.service;

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

        if (!vendor.getPassword()
                .equals(request.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid password"
            );
        }

        return vendor;
    }
}