package com.app.hourbook.controller.vendor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.dto.vendor.VendorRegisterOtpDto;
import com.app.hourbook.dto.vendor.VendorRegisterRequestDto;
import com.app.hourbook.service.VendorRegisterMvcService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorRegisterMvcController {

    private final VendorRegisterMvcService vendorRegisterMvcService;

    // =========================================================
    // REGISTER PAGE
    // =========================================================

    @GetMapping("/register")
    public String registerPage(
            HttpSession session,
            Model model) {

        if (session.getAttribute("vendorId") != null) {

            return "redirect:/vendor/dashboard";
        }

        if (!model.containsAttribute("vendorRegister")) {

            model.addAttribute(
                    "vendorRegister",
                    new VendorRegisterRequestDto()
            );
        }

        return "vendor/register";
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public String register(
            @Valid
            @ModelAttribute("vendorRegister")
            VendorRegisterRequestDto request,

            BindingResult result,

            HttpSession session,

            Model model) {

        if (result.hasErrors()) {

            return "vendor/register";
        }

        try {

            Integer vendorId =
                    vendorRegisterMvcService
                            .register(request);

            session.setAttribute(
                    "vendorRegistrationId",
                    vendorId
            );

            session.setAttribute(
                    "vendorRegistrationEmail",
                    request.getEmail()
            );

            return "redirect:/vendor/verify-registration";

        } catch (RuntimeException ex) {

            log.error("Vendor registration failed: {}", ex.getMessage(), ex);

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "vendor/register";

        } catch (Exception ex) {

            log.error("Unexpected error during vendor registration", ex);

            model.addAttribute(
                    "error",
                    "Unable to complete registration. Please try again."
            );

            return "vendor/register";
        }
    }

    // =========================================================
    // OTP PAGE
    // =========================================================

    @GetMapping("/verify-registration")
    public String verifyRegistrationPage(
            HttpSession session,
            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute(
                        "vendorRegistrationId"
                );

        if (vendorId == null) {

            return "redirect:/vendor/register";
        }

        model.addAttribute(
                "otpRequest",
                new VendorRegisterOtpDto()
        );

        model.addAttribute(
                "email",
                session.getAttribute(
                        "vendorRegistrationEmail"
                )
        );

        return "vendor/verify-registration";
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    @PostMapping("/verify-registration")
    public String verifyRegistration(
            @Valid
            @ModelAttribute("otpRequest")
            VendorRegisterOtpDto request,

            BindingResult result,

            HttpSession session,

            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute(
                        "vendorRegistrationId"
                );

        if (vendorId == null) {

            return "redirect:/vendor/register";
        }

        if (result.hasErrors()) {

            model.addAttribute(
                    "email",
                    session.getAttribute(
                            "vendorRegistrationEmail"
                    )
            );

            return "vendor/verify-registration";
        }

        try {

            vendorRegisterMvcService.verifyOtp(
                    vendorId,
                    request.getOtp()
            );

            session.removeAttribute(
                    "vendorRegistrationId"
            );

            session.removeAttribute(
                    "vendorRegistrationEmail"
            );

            return "redirect:/vendor/login?registered=true";

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            model.addAttribute(
                    "email",
                    session.getAttribute(
                            "vendorRegistrationEmail"
                    )
            );

            return "vendor/verify-registration";
        }
    }
}