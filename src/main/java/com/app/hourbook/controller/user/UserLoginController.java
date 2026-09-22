package com.app.hourbook.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.dto.user.UserLoginRequestDto;
import com.app.hourbook.exception.InvalidCredentialsException;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.service.UserLoginService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;

    @GetMapping("/login")
    public String loginPage(
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/user/dashboard";
        }

        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute(
                    "loginRequest",
                    new UserLoginRequestDto()
            );
        }

        return "user/login";
    }

    @PostMapping("/login")
    public String login(

            @Valid
            @ModelAttribute("loginRequest")
            UserLoginRequestDto request,

            BindingResult result,

            HttpSession session,

            Model model) {

        if (result.hasErrors()) {
            return "user/login";
        }

        try {

            AppUserDetails user =
                    userLoginService.login(request);

            session.setAttribute(
                    "userId",
                    user.getId()
            );

            session.setAttribute(
                    "userName",
                    user.getFullName()
            );

            session.setAttribute(
                    "userEmail",
                    user.getEmail()
            );

            session.setAttribute(
                    "profileImage",
                    user.getProfileImage()
            );

            return "redirect:/user/dashboard";

        } catch (InvalidCredentialsException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "user/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/user/login";
    }
}