package com.app.hourbook.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.VendorDetailsRequestDto;
import com.app.hourbook.dto.VendorDetailsResponseDto;
import com.app.hourbook.dto.VerifyOtp;
import com.app.hourbook.security.JwtService;
import com.app.hourbook.service.AppUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appUserDetails")
public class AppUserDetailsController {
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private AppUserDetailsService auds;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> save(@Valid @RequestBody VendorDetailsRequestDto vd,HttpServletRequest req) throws Exception{
		
		Long lid= jwtService.getVendorId(req);
		Integer id = lid.intValue();
		return ResponseEntity.ok(auds.createUser( vd,id));
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse<VendorDetailsResponseDto>> verifyOtp( @Valid @RequestBody VerifyOtp req,HttpServletRequest httpReq){
		Long lid = jwtService.getVendorId(httpReq);
		Integer id = lid.intValue();
	     ApiResponse<VendorDetailsResponseDto> res=	auds.verifyOtp(req, id);
	     return ResponseEntity.ok(res);
	}
}
