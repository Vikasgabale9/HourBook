package com.app.hourbook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.user.UserDashboardResponseDto;
import com.app.hourbook.exception.UserNotFoundException;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.repository.AppUserDetailsRepository;


@Service
public class UserDashboardService {
	
	@Autowired
	AppUserDetailsRepository userRepo;
	
	public UserDashboardResponseDto getDashboard(Integer id) {
		AppUserDetails user= userRepo.findById(id).orElseThrow(()-> new UserNotFoundException("User not found with id : "+id));
		
		return entityToDto(user); 
	}
	public UserDashboardResponseDto entityToDto(AppUserDetails user) {
		
		UserDashboardResponseDto dto = new UserDashboardResponseDto();
		dto.setActive(user.isActive());
		dto.setCancelledBookings(10l);
		dto.setCompletedBookings(10l);
		dto.setEmail(user.getEmail());
		dto.setEmailVerified(user.isEmailVerified());
		dto.setFullName(user.getFullName());
		dto.setPhoneNumber(user.getPhoneNumber());
		dto.setPhoneVerified(
                user.getAppUser() != null &&
                user.getAppUser().isVerified()
        );
		dto.setProfileImage("/uploads/vendor/profile/8406c456-2560-4d98-8974-99b811284c27_BoyProfileImage.jpeg");
		dto.setTotalBookings(120l);
		dto.setUpcomingBookings(10l);
		dto.setUserId(user.getId());
		return dto;
	}
}
