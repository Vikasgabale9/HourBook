package com.app.hourbook.controller.vendor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.service.VendorProfileService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorProfileController {

    private final VendorProfileService service;

    @GetMapping("/profile")
    public String profile(Model model,
                          HttpSession session){

        Integer vendorId =
                (Integer)session.getAttribute("vendorId");

        model.addAttribute(
                "profile",
                service.getProfile(vendorId));

        return "vendor/profile";
    }
}