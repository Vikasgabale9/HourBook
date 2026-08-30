package com.app.hourbook.controller.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.app.hourbook.dto.VendorDashLoginReq;
import com.app.hourbook.exception.InvalidCredentialsException;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.service.VendorLoginService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/vendor")
public class VendorLoginController {

    @Autowired
    private VendorLoginService vendorLoginService;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        if (session.getAttribute("vendorId") != null) {
            return "redirect:/vendor/dashboard";
        }

        return "vendor/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute VendorDashLoginReq request,
                        HttpSession session,
                        Model model) {

        try {

            VendorDetails vendor = vendorLoginService.login(request);

            session.setAttribute("vendorId",
                    vendor.getVendorAuth().getId());

            session.setAttribute("vendorName",
                    vendor.getFullName());

            session.setAttribute("vendorEmail",
                    vendor.getEmail());

            return "redirect:/vendor/dashboard";

        } catch (InvalidCredentialsException ex) {

            model.addAttribute("error", ex.getMessage());

            return "vendor/login";
        }

    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/vendor/login";
    }

}