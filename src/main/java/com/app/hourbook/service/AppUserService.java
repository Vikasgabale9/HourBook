package com.app.hourbook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.UserLoginRequest;
import com.app.hourbook.dto.UserLoginResponse;
import com.app.hourbook.dto.VerifyOtpRequest;
import com.app.hourbook.dto.VerifyOtpResponse;
import com.app.hourbook.email.EmailService;
import com.app.hourbook.enums.Role;
import com.app.hourbook.exception.UserNotFoundException;
import com.app.hourbook.model.AppUser;
import com.app.hourbook.repository.AppUserRepository;
import com.app.hourbook.security.JwtService;

@Service
public class AppUserService {

	private AppUserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private EmailService es;

	public ApiResponse<UserLoginResponse> login(UserLoginRequest req) {
		String otp = es.genrateOtp();
		UserLoginResponse res = new UserLoginResponse();
		res.setOtp(otp);
		if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
			AppUser user = userRepository.findByPhoneNumber(req.getPhoneNumber()).get();
			res.setPhoneNumber(req.getPhoneNumber());
			user.setOtp(otp);
			userRepository.save(user);
			return ApiResponse.success("OTP sent successfully : ", 201, res);
		}

		res.setPhoneNumber(req.getPhoneNumber());
		userRepository.save(dtoToEntity(req, otp));
		return ApiResponse.success("OTP sent successfully : ", 201, res);
	}

	public AppUser dtoToEntity(UserLoginRequest req, String otp) {
		AppUser user = new AppUser();
		user.setOtp(otp);
		user.setPhoneNumber(req.getPhoneNumber());
		user.setRole(Role.ROLE_USER);

		return user;
	}

	public AppUserService(AppUserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	public ApiResponse<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request) {

		AppUser user = userRepository.findByPhoneNumber(request.phoneNumber())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		if (user.getOtp() == null) {
			return ApiResponse.error("OTP expired or already used", 400, null);
		}

		if (!user.getOtp().equals(request.otp())) {
			return ApiResponse.error("Invalid OTP", 400, null);
		}

		String token = jwtService.generateToken(user.getId().longValue(),
				"USER", 
				user.getPhoneNumber(),
				user.getRole());
		
		user.setOtp(null);
		user.setVerified(true);
		user.setToken(token);
		userRepository.save(user);

		VerifyOtpResponse response = new VerifyOtpResponse(user.getId(), user.getPhoneNumber(), user.getToken());

		return ApiResponse.success("OTP verified successfully", 200, response);
	}
}