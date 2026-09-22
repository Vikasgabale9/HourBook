package com.app.hourbook.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.service.UserProfileService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


import java.io.IOException;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.app.hourbook.dto.user.UserProfileUpdateDto;

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
    
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute UserProfileUpdateDto request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        try {

            userProfileService.updateProfile(
                    userId,
                    request
            );

            // Update session name/email
            session.setAttribute(
                    "userName",
                    request.getFullName()
            );

            session.setAttribute(
                    "userEmail",
                    request.getEmail()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profile updated successfully"
            );

            return "redirect:/user/profile";

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to upload profile image"
            );

            return "redirect:/user/profile/edit";
        }
    }

}