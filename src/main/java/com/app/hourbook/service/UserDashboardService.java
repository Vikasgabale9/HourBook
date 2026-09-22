package com.app.hourbook.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.user.UserDashboardResponseDto;
import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.exception.UserNotFoundException;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.repository.AppUserDetailsRepository;
import com.app.hourbook.repository.BookingRepository;


@Service
public class UserDashboardService {
	
	@Autowired
	private AppUserDetailsRepository userRepo;
	
	@Autowired
	private BookingRepository bookingRepo;
	
	
	public UserDashboardResponseDto getDashboard(Integer id) {
		AppUserDetails user= userRepo.findById(id).orElseThrow(()-> new UserNotFoundException("User not found with id : "+id));
		
		return entityToDto(user); 
	}
	public UserDashboardResponseDto entityToDto(AppUserDetails user) {
		
		UserDashboardResponseDto dto = new UserDashboardResponseDto();
		dto.setActive(user.isActive());
		dto.setCancelledBookings(bookingRepo.countByUser_IdAndStatus(user.getId(), BookingStatus.CANCELLED));
		dto.setCompletedBookings(bookingRepo.countByUser_IdAndStatus(user.getId(), BookingStatus.COMPLETED));
		dto.setEmail(user.getEmail());
		dto.setEmailVerified(user.isEmailVerified());
		dto.setFullName(user.getFullName());
		dto.setPhoneNumber(user.getPhoneNumber());
		dto.setPhoneVerified(
                user.getAppUser() != null &&
                user.getAppUser().isVerified()
        );
		dto.setProfileImage(user.getProfileImage());
		dto.setTotalBookings(bookingRepo.countByUser_Id(user.getId()));
		dto.setUpcomingBookings(bookingRepo.countByUser_IdAndBookingDateGreaterThanEqualAndStatus(user.getId(), LocalDate.now(),BookingStatus.CONFIRMED ));
		dto.setUserId(user.getId());
		return dto;
	}
}
