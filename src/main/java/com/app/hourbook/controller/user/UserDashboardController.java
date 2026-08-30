package com.app.hourbook.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.dto.user.UserDashboardResponseDto;
import com.app.hourbook.service.UserDashboardService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserDashboardService userDashboardService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session,
                            Model model) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        UserDashboardResponseDto dashboard =
                userDashboardService.getDashboard(userId);

        model.addAttribute("dashboard", dashboard);

        return "user/dashboard";
    }

}