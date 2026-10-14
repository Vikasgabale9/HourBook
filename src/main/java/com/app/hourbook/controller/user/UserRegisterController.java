package com.app.hourbook.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.hourbook.dto.user.UserRegisterOtpDto;
import com.app.hourbook.dto.user.UserRegisterRequestDto;
import com.app.hourbook.service.UserRegisterService;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserRegisterController {

    private final UserRegisterService userRegisterService;

    @GetMapping("/register")
    public String registerPage(
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/user/dashboard";
        }

        if (!model.containsAttribute("userRegister")) {

            model.addAttribute(
                    "userRegister",
                    new UserRegisterRequestDto()
            );
        }

        return "user/register";
    }

    @PostMapping("/register")
    public String register(

            @Valid
            @ModelAttribute("userRegister")
            UserRegisterRequestDto request,

            BindingResult result,

            HttpSession session,
            Model model) {

        if (result.hasErrors()) {
            return "user/register";
        }

        try {

            Integer userId =
                    userRegisterService.register(request);

            session.setAttribute(
                    "registrationUserId",
                    userId
            );

            session.setAttribute(
                    "registrationEmail",
                    request.getEmail()
            );

            return "redirect:/user/verify-registration";

        } catch (RuntimeException ex) {

            log.error("User registration failed: {}", ex.getMessage(), ex);

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "user/register";

        } catch (Exception ex) {

            log.error("Unexpected error during user registration", ex);

            model.addAttribute(
                    "error",
                    "Unable to complete registration. Please try again."
            );

            return "user/register";
        }
    }

    @GetMapping("/verify-registration")
    public String verifyRegistrationPage(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute(
                        "registrationUserId"
                );

        if (userId == null) {
            return "redirect:/user/register";
        }

        model.addAttribute(
                "otpRequest",
                new UserRegisterOtpDto()
        );

        model.addAttribute(
                "email",
                session.getAttribute(
                        "registrationEmail"
                )
        );

        return "user/verify-registration";
    }

    @PostMapping("/verify-registration")
    public String verifyRegistration(

            @Valid
            @ModelAttribute("otpRequest")
            UserRegisterOtpDto request,

            BindingResult result,

            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute(
                        "registrationUserId"
                );

        if (userId == null) {
            return "redirect:/user/register";
        }

        if (result.hasErrors()) {

            model.addAttribute(
                    "email",
                    session.getAttribute(
                            "registrationEmail"
                    )
            );

            return "user/verify-registration";
        }

        try {

            userRegisterService.verifyOtp(
                    userId,
                    request.getOtp()
            );

            session.removeAttribute(
                    "registrationUserId"
            );

            session.removeAttribute(
                    "registrationEmail"
            );

            return "redirect:/user/login?registered=true";

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            model.addAttribute(
                    "email",
                    session.getAttribute(
                            "registrationEmail"
                    )
            );

            return "user/verify-registration";
        }
    }
}