package com.app.hourbook.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminLoginRequest {

    @NotBlank(message = "email is required")
    private String email;

    
    @NotBlank(message = "Password is required")
    private String password;
}