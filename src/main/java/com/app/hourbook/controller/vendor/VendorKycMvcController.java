package com.app.hourbook.controller.vendor;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.dto.VendorKycRequestDto;
import com.app.hourbook.dto.VendorKycResponseDto;
import com.app.hourbook.service.VendorDetailsService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorKycMvcController {

    private final VendorDetailsService vendorDetailsService;

    // =========================================================
    // KYC PAGE
    // =========================================================

    @GetMapping("/kyc")
    public String vendorKyc(
            HttpSession session,
            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {

            return "redirect:/vendor/login";
        }

        VendorKycResponseDto vendor =
                vendorDetailsService.getKyc(vendorId);

        model.addAttribute(
                "vendor",
                vendor
        );

        if (!model.containsAttribute("kycRequest")) {

            VendorKycRequestDto request =
                    new VendorKycRequestDto();

            request.setFullName(
                    vendor.getFullName()
            );

            request.setPhoneNumber(
                    vendor.getPhoneNumber()
            );

            model.addAttribute(
                    "kycRequest",
                    request
            );
        }

        return "vendor/kyc";
    }

    // =========================================================
    // SUBMIT KYC
    // =========================================================

    @PostMapping("/kyc")
    public String submitKyc(
            @Valid
            @ModelAttribute("kycRequest")
            VendorKycRequestDto request,

            BindingResult result,

            HttpSession session,

            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {

            return "redirect:/vendor/login";
        }

        if (result.hasErrors()) {

            VendorKycResponseDto vendor =
                    vendorDetailsService.getKyc(vendorId);

            model.addAttribute(
                    "vendor",
                    vendor
            );

            return "vendor/kyc";
        }

        try {

            vendorDetailsService.submitKyc(
                    vendorId,
                    request
            );

            // Update profile image in session
            VendorKycResponseDto updated =
                    vendorDetailsService.getKyc(
                            vendorId
                    );

            session.setAttribute(
                    "profileImage",
                    updated.getProfileImage()
            );

            session.setAttribute(
                    "vendorName",
                    updated.getFullName()
            );

            return "redirect:/vendor/kyc?success=true";

        } catch (IOException | RuntimeException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            VendorKycResponseDto vendor =
                    vendorDetailsService.getKyc(vendorId);

            model.addAttribute(
                    "vendor",
                    vendor
            );

            return "vendor/kyc";
        }
    }

    // =========================================================
    // UPDATE KYC
    // =========================================================

    @PostMapping("/kyc/update")
    public String updateKyc(
            @Valid
            @ModelAttribute("kycRequest")
            VendorKycRequestDto request,

            BindingResult result,

            HttpSession session,

            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {

            return "redirect:/vendor/login";
        }

        if (result.hasErrors()) {

            VendorKycResponseDto vendor =
                    vendorDetailsService.getKyc(vendorId);

            model.addAttribute(
                    "vendor",
                    vendor
            );

            return "vendor/kyc";
        }

        try {

            vendorDetailsService.updateKyc(
                    vendorId,
                    request
            );

            VendorKycResponseDto updated =
                    vendorDetailsService.getKyc(
                            vendorId
                    );

            session.setAttribute(
                    "profileImage",
                    updated.getProfileImage()
            );

            session.setAttribute(
                    "vendorName",
                    updated.getFullName()
            );

            return "redirect:/vendor/kyc?updated=true";

        } catch (IOException | RuntimeException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            VendorKycResponseDto vendor =
                    vendorDetailsService.getKyc(vendorId);

            model.addAttribute(
                    "vendor",
                    vendor
            );

            return "vendor/kyc";
        }
    }
}