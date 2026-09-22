package com.app.hourbook.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.user.UserProfileUpdateDto;
import com.app.hourbook.exception.UserNotFoundException;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.repository.AppUserDetailsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final AppUserDetailsRepository userRepository;

    public AppUserDetails getProfile(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }

    public UserProfileUpdateDto getProfileForEdit(Integer userId) {

        AppUserDetails user = getProfile(userId);

        UserProfileUpdateDto dto = new UserProfileUpdateDto();

        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    public void updateProfile(
            Integer userId,
            UserProfileUpdateDto request)
            throws IOException {

        AppUserDetails user = getProfile(userId);

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        /*
         * Update profile image only when a new image is selected.
         * Existing image remains unchanged otherwise.
         */
        MultipartFile image = request.getProfileImage();

        if (image != null && !image.isEmpty()) {

            String imagePath = uploadProfileImage(image);

            user.setProfileImage(imagePath);
        }

        userRepository.save(user);
    }

    private String uploadProfileImage(
            MultipartFile image) throws IOException {

        String fileName =
                UUID.randomUUID()
                        + "_"
                        + image.getOriginalFilename();

        Path uploadPath =
                Paths.get("uploads/user/profile");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath =
                uploadPath.resolve(fileName);

        Files.copy(
                image.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "/uploads/user/profile/" + fileName;
    }
}