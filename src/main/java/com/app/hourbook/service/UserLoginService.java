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

        if (request.getLoginId() == null ||
                request.getLoginId().isBlank()) {

            throw new InvalidCredentialsException(
                    "Email or phone number is required"
            );
        }

        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new InvalidCredentialsException(
                    "Password is required"
            );
        }

        AppUserDetails user;

        /*
         * Email login
         */
        if (request.getLoginId().contains("@")) {

            user = userRepository
                    .findByEmail(request.getLoginId())
                    .orElseThrow(() ->
                            new InvalidCredentialsException(
                                    "Invalid email or password"
                            )
                    );

        }

        /*
         * Phone login
         */
        else {

            user = userRepository
                    .findByAppUserPhoneNumber(
                            request.getLoginId()
                    )
                    .orElseThrow(() ->
                            new InvalidCredentialsException(
                                    "Invalid phone number or password"
                            )
                    );
        }

        /*
         * Account active check
         */
        if (!user.isActive()) {

            throw new InvalidCredentialsException(
                    "Your account is inactive."
            );
        }

        /*
         * Email verification check
         */
        if (!user.isEmailVerified()) {

            throw new InvalidCredentialsException(
                    "Please verify your email first."
            );
        }

        /*
         * Mobile verification check
         */
        if (!user.getAppUser().isVerified()) {

            throw new InvalidCredentialsException(
                    "Please verify your mobile number."
            );
        }

        /*
         * PASSWORD CHECK
         *
         * Password is compared with the password
         * stored in AppUserDetails.
         */
        if (!request.getPassword()
                .equals(user.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email/phone number or password"
            );
        }

        return user;
    }
}