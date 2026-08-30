package com.app.hourbook.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.hourbook.dto.AdminLoginResponse;
import com.app.hourbook.dto.admin.AdminRegisterRequest;
import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.VerifyOtp;
import com.app.hourbook.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminRegisterController {

    private final AdminService adminService;

    public AdminRegisterController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> register(
            @RequestBody AdminRegisterRequest request) throws Exception {

        return ResponseEntity.ok( adminService.register(request));
    }
    
    @PostMapping("/verify-adminotp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(
            @RequestBody VerifyOtp request) {

        return ResponseEntity.ok(
                adminService.verifyOtp(request)
        );
    }
}