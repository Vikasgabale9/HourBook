package com.app.hourbook.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginResponse {

	private String otp;
	
	private String phoneNumber;
}
