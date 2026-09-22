package com.app.hourbook.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequestDto {

    @NotBlank(message = "Email or phone number is required")
    private String loginId;

    @NotBlank(message = "Password is required")
    private String password;
}