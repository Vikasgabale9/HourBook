package com.app.hourbook.controller.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.UserLoginRequest;
import com.app.hourbook.dto.UserLoginResponse;
import com.app.hourbook.dto.VerifyOtpRequest;
import com.app.hourbook.dto.VerifyOtpResponse;
import com.app.hourbook.security.JwtService;
import com.app.hourbook.service.VendorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

	private VendorService vendorService;
	
	@Autowired
	private JwtService  jwtService;

	public VendorController(VendorService vendorService) {
		super();
		this.vendorService = vendorService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UserLoginResponse>> register( @Valid @RequestBody UserLoginRequest req){
		
	     ApiResponse<UserLoginResponse> res=	vendorService.login(req);
	     return ResponseEntity.ok(res);
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp( @Valid @RequestBody VerifyOtpRequest req){
		
	     ApiResponse<VerifyOtpResponse> res=	vendorService.verifyOtp(req);
	     return ResponseEntity.ok(res);
	}
	@GetMapping("/home")
	public String home(@RequestParam String token) {
		String role =jwtService.extractRole(token);
		return "Vendor home : "+role;
	}
}
