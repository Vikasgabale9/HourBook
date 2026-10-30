package com.app.hourbook.controller.vendor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // =====================================================
    // GET VENDOR KYC PAGE
    // =====================================================

    @GetMapping("/kyc")
    public String vendorKyc(
            HttpSession session,
            Model model) {

        Integer vendorId = getVendorId(session);

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorKycResponseDto vendor =
                vendorDetailsService.getKyc(vendorId);

        model.addAttribute("vendor", vendor);

        if (!model.containsAttribute("kycRequest")) {

            VendorKycRequestDto request =
                    new VendorKycRequestDto();

            request.setFullName(vendor.getFullName());
            request.setPhoneNumber(vendor.getPhoneNumber());
            request.setAadhaarNumber(vendor.getAadhaarNumber());
            request.setPanNumber(vendor.getPanNumber());
            request.setDateOfBirth(vendor.getDateOfBirth());

            model.addAttribute("kycRequest", request);
        }

        return "vendor/kyc";
    }

    // =====================================================
    // SUBMIT NEW KYC
    // =====================================================

    @PostMapping("/kyc")
    public String submitKyc(
            @Valid
            @ModelAttribute("kycRequest")
            VendorKycRequestDto request,

            BindingResult result,

            HttpSession session,

            Model model,

            RedirectAttributes redirectAttributes) {

        Integer vendorId = getVendorId(session);

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorKycResponseDto vendor =
                vendorDetailsService.getKyc(vendorId);

        // Validate required documents for first submission.
        if (request.getAadhaarImage() == null ||
                request.getAadhaarImage().isEmpty()) {

            result.rejectValue(
                    "aadhaarImage",
                    "aadhaarImage.required",
                    "Please upload your Aadhaar document."
            );
        }

        if (request.getPanImage() == null ||
                request.getPanImage().isEmpty()) {

            result.rejectValue(
                    "panImage",
                    "panImage.required",
                    "Please upload your PAN document."
            );
        }

        if (result.hasErrors()) {
            model.addAttribute("vendor", vendor);
            return "vendor/kyc";
        }

        try {

            VendorKycResponseDto updated =
                    vendorDetailsService.submitKyc(
                            vendorId,
                            request
                    );

            updateVendorSession(session, updated);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "KYC submitted successfully. Your documents are pending verification."
            );

            return "redirect:/vendor/kyc";

        } catch (Exception ex) {

            model.addAttribute("vendor", vendor);

            model.addAttribute(
                    "error",
                    getErrorMessage(ex)
            );

            return "vendor/kyc";
        }
    }

    // =====================================================
    // UPDATE EXISTING KYC
    // =====================================================

    @PostMapping("/kyc/update")
    public String updateKyc(
            @Valid
            @ModelAttribute("kycRequest")
            VendorKycRequestDto request,

            BindingResult result,

            HttpSession session,

            Model model,

            RedirectAttributes redirectAttributes) {

        Integer vendorId = getVendorId(session);

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorKycResponseDto vendor =
                vendorDetailsService.getKyc(vendorId);

        if (result.hasErrors()) {
            model.addAttribute("vendor", vendor);
            return "vendor/kyc";
        }

        try {

            VendorKycResponseDto updated =
                    vendorDetailsService.updateKyc(
                            vendorId,
                            request
                    );

            updateVendorSession(session, updated);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "KYC details updated successfully."
            );

            return "redirect:/vendor/kyc";

        } catch (Exception ex) {

            model.addAttribute("vendor", vendor);

            model.addAttribute(
                    "error",
                    getErrorMessage(ex)
            );

            return "vendor/kyc";
        }
    }

    // =====================================================
    // SESSION HELPERS
    // =====================================================

    private Integer getVendorId(HttpSession session) {

        Object vendorId = session.getAttribute("vendorId");

        if (vendorId instanceof Integer) {
            return (Integer) vendorId;
        }

        return null;
    }

    private void updateVendorSession(
            HttpSession session,
            VendorKycResponseDto vendor) {

        session.setAttribute(
                "vendorName",
                vendor.getFullName()
        );

        session.setAttribute(
                "vendorEmail",
                vendor.getEmail()
        );

        session.setAttribute(
                "profileImage",
                vendor.getProfileImage()
        );
    }

    private String getErrorMessage(Exception ex) {

        String message = ex.getMessage();

        if (message == null || message.isBlank()) {
            return "Unable to process KYC. Please try again.";
        }

        return message;
    }
}
