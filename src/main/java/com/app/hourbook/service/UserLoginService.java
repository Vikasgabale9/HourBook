package com.app.hourbook.service;

import org.springframework.stereotype.Service;

import com.app.hourbook.dto.user.UserLoginRequestDto;
import com.app.hourbook.exception.InvalidCredentialsException;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.repository.AppUserDetailsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserLoginService {

    private final AppUserDetailsRepository userRepository;

  

    public AppUserDetails login(UserLoginRequestDto request) {

    	AppUserDetails user;

    	if (request.getLoginId().contains("@")) {

    	    user = userRepository.findByEmail(request.getLoginId())
    	            .orElseThrow(() ->
    	                    new InvalidCredentialsException("Invalid Email"));

    	} else {

    	    user = userRepository.findByAppUserPhoneNumber(request.getLoginId())
    	            .orElseThrow(() ->
    	                    new InvalidCredentialsException("Invalid Phone Number"));
    	}

        if (!user.isActive()) {

            throw new InvalidCredentialsException("Your account is inactive.");
        }

        if (!user.isEmailVerified()) {

            throw new InvalidCredentialsException("Please verify your email first.");
        }

        if (!user.getAppUser().isVerified()) {

            throw new InvalidCredentialsException("Please verify your mobile number.");
        }

        return user;
    }

}