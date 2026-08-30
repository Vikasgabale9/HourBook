package com.app.hourbook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.ApiResponse;
import com.app.hourbook.dto.VendorDetailsRequestDto;
import com.app.hourbook.dto.VendorDetailsResponseDto;
import com.app.hourbook.dto.VerifyOtp;
import com.app.hourbook.email.EmailService;
import com.app.hourbook.model.AppUser;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.repository.AppUserDetailsRepository;
import com.app.hourbook.repository.AppUserRepository;

@Service
public class AppUserDetailsService {
	
	@Autowired
	private AppUserRepository appUserRepository;
	
	@Autowired
	private AppUserDetailsRepository appUserDetailsRepository;
	
	@Autowired
	private EmailService emailService;
	
	
	public ApiResponse<String> createUser(VendorDetailsRequestDto request,Integer id) throws Exception{
		
	   AppUser appUser= appUserRepository.findById(id)
		              .orElseThrow(() ->
                      new RuntimeException(
                              "AppUser not found"));
		
	  if(!appUser.getPhoneNumber().equals(request.getPhoneNumber()) ) {
		  throw new RuntimeException("PhoneNumber not matching with Token");
	  }
	  
	          String otp = emailService.genrateOtp();
	          
	          AppUserDetails appUserDetails = new AppUserDetails();
	          
	          appUserDetails.setAppUser(appUser);
	          appUserDetails.setEmail(request.getEmail());
	          appUserDetails.setFullName(request.getFullName());
	          appUserDetails.setPassword(request.getPassword());
	          appUserDetails.setOtp(otp);
	          appUserDetails.setPhoneNumber(request.getPhoneNumber());
	          
	          appUserDetailsRepository.save(appUserDetails);
	          emailService.sendOtpEmail(request.getEmail(), otp);
	          
	          return ApiResponse.success("Profile saved successfully.", 201 ,"Verify email otp is shared on email." );
	}
	
	public ApiResponse<VendorDetailsResponseDto> verifyOtp(VerifyOtp request,Integer id){
		
		AppUserDetails appUser = appUserDetailsRepository.findByAppUserId(id).get();
		if(!appUser.getEmail().equals(request.email())) {
			throw new RuntimeException("Token details are is not matching with VendorDetails.");
		}
		
		if(!request.otp().equals(appUser.getOtp())) {
			throw new RuntimeException("Invalid otp");
		}
		
		appUser.setEmailVerified(true);
		appUser.setOtp(null);
	appUser = appUserDetailsRepository.save(appUser);
		VendorDetailsResponseDto res= mapToResponseDto(appUser);
		return  ApiResponse.success("Email verified successfully", 0, res);
		
	}
	   
    private VendorDetailsResponseDto mapToResponseDto(
            AppUserDetails appDetails) {

        VendorDetailsResponseDto dto =
                new VendorDetailsResponseDto();
        
        dto.setEmail(appDetails.getEmail());
        dto.setFullName(appDetails.getFullName());
        dto.setId(appDetails.getId());
        

        if (appDetails.getAppUser() != null) {

            dto.setAuthId(
            		appDetails.getAppUser().getId());

            dto.setPhoneNumber(
            		appDetails.getAppUser().getPhoneNumber());
        }
        
        return dto;
    }

}
