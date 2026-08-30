package com.app.hourbook.service;

import java.io.IOException;
import org.springframework.stereotype.Service;

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

      

        userRepository.save(user);

    }

    
}