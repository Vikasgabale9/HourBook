
package com.app.hourbook.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final CloudinaryImageService cloudinaryImageService;

    @Transactional(readOnly = true)
    public AppUserDetails getProfile(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileUpdateDto getProfileForEdit(Integer userId) {
        AppUserDetails user = getProfile(userId);

        UserProfileUpdateDto dto = new UserProfileUpdateDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    @Transactional
    public void updateProfile(
            Integer userId,
            UserProfileUpdateDto request) throws IOException {

        AppUserDetails user = getProfile(userId);

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        MultipartFile image = request.getProfileImage();

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryImageService.uploadImage(image);
            user.setProfileImage(imageUrl);
        }

        userRepository.save(user);
    }
}
