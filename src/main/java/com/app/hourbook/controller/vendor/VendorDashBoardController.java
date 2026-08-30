package com.app.hourbook.controller.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.hourbook.dto.VendorDashboardResponseDto;
import com.app.hourbook.service.VendorDashboardService;

import jakarta.servlet.http.HttpSession;

@Controller
public class VendorDashBoardController {

    @Autowired
    private VendorDashboardService vendorDashboardService;

    @GetMapping("/vendor/dashboard")
    public String dashboard(HttpSession session,
                            Model model) {

        Integer vendorId = (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorDashboardResponseDto dashboard =
                vendorDashboardService.getDashboard(vendorId);

        model.addAttribute("dashboard", dashboard);

        return "vendor/dashboard";
    }

}