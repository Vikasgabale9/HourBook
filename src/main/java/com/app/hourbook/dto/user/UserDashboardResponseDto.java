package com.app.hourbook.dto.user;

import lombok.Data;

@Data
public class UserDashboardResponseDto {

	public UserDashboardResponseDto() {
		// TODO Auto-generated constructor stub
	}
	
	private Integer userId;

	private String fullName;

	private String email;

	private String phoneNumber;

	private Boolean emailVerified;

	private Boolean phoneVerified;

	private Boolean active;

	private String profileImage;

	private Long totalBookings;

	private Long upcomingBookings;

	private Long completedBookings;

	private Long cancelledBookings;

}
