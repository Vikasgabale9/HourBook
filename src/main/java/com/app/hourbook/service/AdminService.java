package com.app.hourbook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.admin.AdminLoginRequest;
import com.app.hourbook.dto.AdminLoginResponse;
import com.app.hourbook.dto.admin.AdminRegisterRequest;
import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.VerifyOtp;
import com.app.hourbook.email.EmailService;
import com.app.hourbook.enums.Role;
import com.app.hourbook.exception.AdminNotFoundException;
import com.app.hourbook.model.AppAdmin;
import com.app.hourbook.repository.AdminRepository;
import com.app.hourbook.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class AdminService {

	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private EmailService emailService;
	

	


public ApiResponse<AdminLoginResponse> login(AdminLoginRequest request) {
    AppAdmin admin = adminRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Invalid username"));

    if (!passwordEncoder.matches(
            request.getPassword(),
            admin.getPassword())) {
        return ApiResponse.error("Invalid password", 401, null);
    }

    if (!admin.isVerified()) {
        return ApiResponse.error(
            "Admin account is not verified", 403, null
        );
    }

    AdminLoginResponse response =
        new AdminLoginResponse(admin.getUsername());

    return ApiResponse.success(
        "Admin logged in successfully", 200, response
    );
}


	public ApiResponse<AdminLoginResponse> register(AdminRegisterRequest request) throws Exception {

		if (adminRepository.existsByUsername(request.getUsername())) {
			return ApiResponse.error("Username already exists", 400, null);
		}

		if (adminRepository.existsByEmail(request.getEmail())) {
			return ApiResponse.error("Email already exists", 400, null);
		}

		String otp = emailService.genrateOtp();
		emailService.sendOtpEmail(request.getEmail(), otp);
		AppAdmin admin = new AppAdmin();
		admin.setUsername(request.getUsername());
		admin.setPassword(
			    passwordEncoder.encode(request.getPassword())
			);
		admin.setEmail(request.getEmail());
        admin.setOtp(otp);
        admin.setRole(Role.ROLE_ADMIN);
        
		AdminLoginResponse al = new AdminLoginResponse(request.getEmail());
		adminRepository.save(admin);

		return ApiResponse.success("Admin registered successfully", 201, al);
	}
	
	public ApiResponse<String> verifyOtp(VerifyOtp request) {

	    AppAdmin admin = adminRepository
	            .findByEmail(request.email())
	            .orElseThrow(() ->
	                    new AdminNotFoundException("Admin not found"));
	    

	    if (admin.isVerified()) {
	        return ApiResponse.error(
	                "Admin already verified",
	                400,
	                null
	        );
	    }

	    if (!admin.getOtp().equals(request.otp())) {
	        return ApiResponse.error(
	                "Invalid OTP",
	                400,
	                null
	        );
	    }
		String token =
		        jwtService.generateToken(
		                admin.getId().longValue(),
		                "ADMIN",
		                admin.getEmail(),
		                admin.getRole());
		
		admin.setToken(token);
	    admin.setVerified(true);
	    admin.setOtp(null);

	    adminRepository.save(admin);

	    return ApiResponse.success(
	            "Admin verified successfully",
	            200,
	            "Verification completed"
	    );
	}

public void createDefaultAdminsIfMissing() {
    createDefaultAdmin(
        "admin1",
        "admin@hourbook.com",
        "admin@123"
    );

    createDefaultAdmin(
        "admin2",
        "vikasgabale@hourbook.com",
        "admin@123"
    );

    createDefaultAdmin(
        "admin3",
        "vikas@hourbook.com",
        "admin@123"
    );
}

private void createDefaultAdmin(
        String username,
        String email,
        String rawPassword) {

    if (adminRepository.existsByUsernameOrEmail(username, email)) {
        System.out.println(
            "Default admin already exists: " + username
        );
        return;
    }

    AppAdmin admin = new AppAdmin();
    admin.setUsername(username);
    admin.setEmail(email);
    admin.setPassword(passwordEncoder.encode(rawPassword));
    admin.setRole(Role.ROLE_ADMIN);
    admin.setVerified(true);
    admin.setOtp(null);
    admin.setToken(null);

    adminRepository.save(admin);

    System.out.println("Created default admin: " + username);
}

}
