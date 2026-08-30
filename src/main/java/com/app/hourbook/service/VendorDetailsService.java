package com.app.hourbook.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.InputMismatchException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.VendorDetailsRequestDto;
import com.app.hourbook.dto.VendorDetailsResponseDto;
import com.app.hourbook.dto.VendorKycRequestDto;
import com.app.hourbook.dto.VendorKycResponseDto;
import com.app.hourbook.dto.VerifyOtp;
import com.app.hourbook.email.EmailService;
import com.app.hourbook.enums.KycStatus;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.exception.VendorNotFoundException;
import com.app.hourbook.model.Vendor;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;
import com.app.hourbook.repository.VendorRepository;
import com.app.hourbook.security.JwtService;

@Service
public class VendorDetailsService {

	@Autowired
	private VendorRepository vendorRepository;

	@Autowired
	private VendorDetailsRepository vendorDetailsRepository;

	@Autowired
	private EmailService emailService;
	
	@Autowired
	JwtService jwtService;

	public VendorDetails getVendorByPhoneNumber(String phoneNumber) {
	return 	vendorDetailsRepository.findByVendorAuthPhoneNumber(phoneNumber).orElseThrow(()->new VendorNotFoundException("Vendro not found with : "+phoneNumber));
	}
	public ApiResponse<String> createProfile(Integer vendorId, VendorDetailsRequestDto request) throws Exception {

		Vendor vendor = vendorRepository.findById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
		if (!vendor.getPhoneNumber().equals(request.getPhoneNumber())) {
			throw new RuntimeException("PhoneNumber not matching with Token");
		}

		String otp = emailService.genrateOtp();

		VendorDetails details = new VendorDetails();
		details.setEmail(request.getEmail());
		details.setFullName(request.getFullName());
		details.setPassword(request.getPassword());
		details.setPhoneNumber(request.getPhoneNumber());
		details.setOtp(otp);
		details.setVendorAuth(vendor);

		vendorDetailsRepository.save(details);
		emailService.sendOtpEmail(request.getEmail(), otp);

		return ApiResponse.success("Vendor profile created successfully", 201, "Verify otp shared on Email");
	}

	public ApiResponse<VendorDetailsResponseDto> verifyOtp(VerifyOtp request, Integer id) {
		VendorDetails v = vendorDetailsRepository.findByVendorAuthId(id).get();

		if (!request.email().equals(v.getEmail())) {
			throw new RuntimeException("Token details are is not matching with VendorDetails.");
		}
		if (!request.otp().equals(v.getOtp())) {
			throw new RuntimeException("Invalid otp");
		}
		v.setEmailVerified(true);
		v.setOtp(null);
		vendorDetailsRepository.save(v);
		VendorDetailsResponseDto res = mapToResponseDto(v);
		return ApiResponse.success("Email verified successfully.", 201, res);
	}
	
	
	public VendorKycResponseDto submitKyc(Integer vendorId,
	                                      VendorKycRequestDto dto)
	        throws IOException {

	    VendorDetails vendor = vendorDetailsRepository
	            .findByVendorAuthId(vendorId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Vendor not found"));

	    String reqPhone = dto.getPhoneNumber();
	    if(!reqPhone.equals(vendor.getPhoneNumber())) {
	    	throw new InputMismatchException("This token belongs to vendor :"+vendor.getFullName()+" plz enter valid token");
	    }
	    vendor.setFullName(dto.getFullName());

	    vendor.setAadhaarNumber(dto.getAadhaarNumber());
	    vendor.setPanNumber(dto.getPanNumber());
	    vendor.setDateOfBirth(dto.getDateOfBirth());

	    vendor.setAadhaarImage(uploadImage(dto.getAadhaarImage(), "aadhaar"));
	    vendor.setPanImage(uploadImage(dto.getPanImage(), "pan"));
	    vendor.setProfileImage(uploadImage(dto.getProfileImage(), "profile"));

	    vendor.setKycStatus(KycStatus.PENDING);

	    vendorDetailsRepository.save(vendor);

	    return mapToKycResponse(vendor);
	}
	
	
	public VendorKycResponseDto getKyc(Integer vendorId) {

	    VendorDetails vendor = vendorDetailsRepository
	            .findByVendorAuthId(vendorId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Vendor not found"));
	    
	    
	    return mapToKycResponse(vendor);
	}
	

	public VendorKycResponseDto updateKyc(Integer vendorId,
	                                      VendorKycRequestDto dto)
	        throws IOException {

	    VendorDetails vendor = vendorDetailsRepository
	            .findByVendorAuthId(vendorId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Vendor not found"));

	    String reqPhone = dto.getPhoneNumber();
	    if(!reqPhone.equals(vendor.getPhoneNumber())) {
	    	throw new InputMismatchException("This token belongs to vendor :"+vendor.getFullName()+" plz enter valid token");
	    }
	    
	    vendor.setFullName(dto.getFullName());
	   

	    vendor.setAadhaarNumber(dto.getAadhaarNumber());
	    vendor.setPanNumber(dto.getPanNumber());
	    vendor.setDateOfBirth(dto.getDateOfBirth());

	    if (dto.getAadhaarImage() != null &&
	            !dto.getAadhaarImage().isEmpty()) {

	        vendor.setAadhaarImage(
	                uploadImage(dto.getAadhaarImage(), "aadhaar"));
	    }

	    if (dto.getPanImage() != null &&
	            !dto.getPanImage().isEmpty()) {

	        vendor.setPanImage(
	                uploadImage(dto.getPanImage(), "pan"));
	    }

	    if (dto.getProfileImage() != null &&
	            !dto.getProfileImage().isEmpty()) {

	        vendor.setProfileImage(
	                uploadImage(dto.getProfileImage(), "profile"));
	    }

	    vendor.setKycStatus(KycStatus.PENDING);

	    vendorDetailsRepository.save(vendor);

	    return mapToKycResponse(vendor);
	}

	
	
	private VendorDetailsResponseDto mapToResponseDto(VendorDetails vendorDetails) {

		VendorDetailsResponseDto dto = new VendorDetailsResponseDto();

		dto.setEmail(vendorDetails.getEmail());
		dto.setFullName(vendorDetails.getFullName());
		dto.setId(vendorDetails.getId());

		if (vendorDetails.getVendorAuth() != null) {

			dto.setAuthId(vendorDetails.getVendorAuth().getId());

			dto.setPhoneNumber(vendorDetails.getVendorAuth().getPhoneNumber());
		}

		return dto;
	}

	private String uploadImage(MultipartFile image, String folder) throws IOException {

		if (image == null || image.isEmpty()) {
			return null;
		}

		String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

		Path uploadPath = Paths.get("uploads/vendor/" + folder);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

		return "/uploads/vendor/" + folder + "/" + fileName;
	}

	private VendorKycResponseDto mapToKycResponse(VendorDetails vendor) {

	    return VendorKycResponseDto.builder()

	            .vendorId(vendor.getVendorAuth().getId())

	            .fullName(vendor.getFullName())

	            .email(vendor.getEmail())

	            .phoneNumber(vendor.getPhoneNumber())

	            .emailVerified(vendor.isEmailVerified())

	            .aadhaarNumber(vendor.getAadhaarNumber())

	            .aadhaarImage(vendor.getAadhaarImage())

	            .panNumber(vendor.getPanNumber())

	            .panImage(vendor.getPanImage())

	            .dateOfBirth(vendor.getDateOfBirth())

	            .profileImage(vendor.getProfileImage())

	            .kycStatus(
	                    vendor.getKycStatus() == null
	                            ? "Not Submitted"
	                            : vendor.getKycStatus().name())

	            .build();
	}
}
