package com.app.hourbook.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.service.UserProfileService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public String profile(HttpSession session,
                          Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {

            return "redirect:/user/login";
        }

        AppUserDetails user =
                userProfileService.getProfile(userId);

        model.addAttribute("user", user);

        return "user/profile";
    }

    @GetMapping("/edit")
    public String editProfile(HttpSession session,
                              Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {

            return "redirect:/user/login";
        }

        AppUserDetails user =
                userProfileService.getProfile(userId);

        model.addAttribute("user", user);

        return "user/edit-profile";
    }

}