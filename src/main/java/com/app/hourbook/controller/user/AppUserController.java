package com.app.hourbook.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.UserLoginRequest;
import com.app.hourbook.dto.UserLoginResponse;
import com.app.hourbook.dto.VerifyOtpRequest;
import com.app.hourbook.dto.VerifyOtpResponse;
import com.app.hourbook.service.AppUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class AppUserController {

	
	private AppUserService userService;

	public AppUserController(AppUserService userService) {
		super();
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UserLoginResponse>> login(@Valid @RequestBody UserLoginRequest req) {

		ApiResponse<UserLoginResponse> res = userService.login(req);
		return ResponseEntity.ok(res);
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {

		ApiResponse<VerifyOtpResponse> res = userService.verifyOtp(req);
		return ResponseEntity.ok(res);
	}
}
