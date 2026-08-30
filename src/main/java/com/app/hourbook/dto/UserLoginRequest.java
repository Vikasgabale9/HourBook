package com.app.hourbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginRequest {
	 @NotBlank(message = "Phone number is required")
	 
	 @Pattern(
			    regexp = "^[6-9]\\d{9}$",
			    message = "Phone number must be a valid Indian mobile number"
			)
	private String phoneNumber;
}

