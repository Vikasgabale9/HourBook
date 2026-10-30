
package com.app.hourbook.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.user.UserRegisterRequestDto;
import com.app.hourbook.email.EmailService;
import com.app.hourbook.enums.Role;
import com.app.hourbook.model.AppUser;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.repository.AppUserDetailsRepository;
import com.app.hourbook.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserRegisterService {

    private final AppUserRepository appUserRepository;
    private final AppUserDetailsRepository appUserDetailsRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryImageService cloudinaryImageService;

    private static final long MAX_PROFILE_IMAGE_SIZE =
            5 * 1024 * 1024;

    @Transactional
    public Integer register(
            UserRegisterRequestDto request) throws Exception {

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException(
                    "Password and confirm password do not match"
            );
        }

        if (appUserDetailsRepository
                .existsByEmail(request.getEmail())) {

            throw new RuntimeException(
                    "Email is already registered"
            );
        }

        AppUser appUser =
                appUserRepository
                        .findByPhoneNumber(request.getPhoneNumber())
                        .orElse(null);

        if (appUser != null && appUser.isVerified()) {

            throw new RuntimeException(
                    "Phone number is already registered"
            );
        }

        if (appUser == null) {

            appUser = new AppUser();

            appUser.setPhoneNumber(request.getPhoneNumber());
            appUser.setRole(Role.ROLE_USER);

        } else {

            appUser.setPhoneNumber(request.getPhoneNumber());
            appUser.setRole(Role.ROLE_USER);
        }

        String otp = emailService.genrateOtp();

        appUser.setOtp(otp);
        appUser.setVerified(false);
        appUser.setToken(null);

        appUser = appUserRepository.save(appUser);

        AppUserDetails details =
                appUserDetailsRepository
                        .findByAppUserId(appUser.getId())
                        .orElse(null);

        if (details == null) {

            details = new AppUserDetails();
            details.setAppUser(appUser);
        }

        details.setFullName(request.getFullName());
        details.setPhoneNumber(request.getPhoneNumber());
        details.setEmail(request.getEmail());

        details.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        details.setOtp(otp);
        details.setEmailVerified(false);
        details.setActive(true);

        // ==================== PROFILE IMAGE - CLOUDINARY ====================

        MultipartFile profileImage = request.getProfileImage();

        if (profileImage != null && !profileImage.isEmpty()) {

            if (profileImage.getSize() > MAX_PROFILE_IMAGE_SIZE) {

                throw new RuntimeException(
                        "Profile image size must not exceed 5 MB"
                );
            }

            String imageUrl = cloudinaryImageService.uploadImage(
                    profileImage,
                    "hourbook/users/profiles"
            );

            details.setProfileImage(imageUrl);
        }

        appUserDetailsRepository.save(details);

        emailService.sendOtpEmail(
                request.getEmail(),
                otp
        );

        return appUser.getId();
    }

    // ==================== VERIFY OTP ====================

    @Transactional
    public AppUserDetails verifyOtp(
            Integer userId,
            String otp) {

        AppUser appUser =
                appUserRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException("User not found")
                        );

        if (appUser.getOtp() == null) {

            throw new RuntimeException(
                    "OTP expired or already used"
            );
        }

        if (!appUser.getOtp().equals(otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        AppUserDetails details =
                appUserDetailsRepository
                        .findByAppUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User profile not found"
                                )
                        );

        details.setEmailVerified(true);
        details.setOtp(null);

        appUser.setOtp(null);
        appUser.setVerified(true);

        appUserRepository.save(appUser);
        appUserDetailsRepository.save(details);

        return details;
    }
}
