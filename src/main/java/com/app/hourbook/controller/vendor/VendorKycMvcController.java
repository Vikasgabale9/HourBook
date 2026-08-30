package com.app.hourbook.controller.vendor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.hourbook.dto.VendorKycResponseDto;
import com.app.hourbook.service.VendorDetailsService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VendorKycMvcController {

    private final VendorDetailsService vendorDetailsService;

    @GetMapping("/vendor/kyc")
    public String vendorKyc(HttpSession session,
                            Model model) {

        Integer vendorId = (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorKycResponseDto vendor =
                vendorDetailsService.getKyc(vendorId);

        model.addAttribute("vendor", vendor);

        return "vendor/kyc";
    }
}