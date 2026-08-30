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
import com.app.hourbook.model.Vendor;
import com.app.hourbook.repository.VendorRepository;
import com.app.hourbook.security.JwtService;

@Service
public class VendorService {

	@Autowired
	private EmailService es;

	@Autowired
	private JwtService jwtService;

	private VendorRepository vendorRepository;

	public VendorService(VendorRepository vendorRepository) {
		super();
		this.vendorRepository = vendorRepository;
	}

	public ApiResponse<UserLoginResponse> login(UserLoginRequest req) {
		String otp = es.genrateOtp();
		UserLoginResponse res = new UserLoginResponse();
		res.setOtp(otp);
		if (vendorRepository.existsByPhoneNumber(req.getPhoneNumber())) {
			Vendor user = vendorRepository.findByPhoneNumber(req.getPhoneNumber()).get();
			res.setPhoneNumber(req.getPhoneNumber());
			user.setOtp(otp);
			vendorRepository.save(user);
			return ApiResponse.success("OTP sent successfully : ", 201, res);
		}

		res.setPhoneNumber(req.getPhoneNumber());
		vendorRepository.save(DtoToEntity(req, otp));
		return ApiResponse.success("OTP sent successfully : ", 201, res);
	}

	public Vendor DtoToEntity(UserLoginRequest req, String otp) {
		Vendor user = new Vendor();
		user.setOtp(otp);
		user.setPhoneNumber(req.getPhoneNumber());
		user.setRole(Role.ROLE_VENDOR);
		return user;
	}

	public ApiResponse<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request) {

		Vendor vendor = vendorRepository.findByPhoneNumber(request.phoneNumber())
				.orElseThrow(() -> new RuntimeException("Vendor not found"));

		if (vendor.getOtp() == null) {
			return ApiResponse.error("OTP expired or already used", 400, null);
		}

		if (!vendor.getOtp().equals(request.otp())) {
			return ApiResponse.error("Invalid OTP", 400, null);
		}

		vendor.setOtp(null);
		vendor.setVerified(true);

		String token = jwtService.generateToken(vendor.getId().longValue(),
				"VENDOR", vendor.getPhoneNumber(),
				vendor.getRole());

		vendor.setToken(token);

		vendorRepository.save(vendor);

		VerifyOtpResponse response =
				new VerifyOtpResponse(vendor.getId(), vendor.getPhoneNumber(), token);

		return ApiResponse.success("OTP verified successfully", 200, response);
	}
}
