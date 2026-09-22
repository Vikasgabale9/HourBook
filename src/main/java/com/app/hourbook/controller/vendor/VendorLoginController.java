package com.app.hourbook.controller.vendor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.dto.VendorDashLoginReq;
import com.app.hourbook.exception.InvalidCredentialsException;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.service.VendorMvcLoginService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorLoginController {

    private final VendorMvcLoginService vendorMvcLoginService;

    // =========================================================
    // LOGIN PAGE
    // =========================================================

    @GetMapping("/login")
    public String loginPage(
            HttpSession session,
            Model model) {

        if (session.getAttribute("vendorId") != null) {

            return "redirect:/vendor/dashboard";
        }

        return "vendor/login";
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public String login(
            @ModelAttribute VendorDashLoginReq request,
            HttpSession session,
            Model model) {

        try {

            VendorDetails vendor =
                    vendorMvcLoginService.login(request);

            // -------------------------------------------------
            // Create MVC session
            // -------------------------------------------------

            session.setAttribute(
                    "vendorId",
                    vendor.getVendorAuth().getId()
            );

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

            return "redirect:/vendor/dashboard";

        } catch (InvalidCredentialsException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "vendor/login";
        }
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/vendor/login";
    }
}