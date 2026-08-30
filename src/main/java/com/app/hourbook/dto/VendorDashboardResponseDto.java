package com.app.hourbook.dto;

import com.app.hourbook.enums.KycStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorDashboardResponseDto {
	private Integer vendorId;

	private String vendorName;

	private String vendorEmail;

	private String phoneNumber;

	private Boolean emailVerified;

	private Boolean phoneVerified;

	private Boolean vendorActive;

	private KycStatus kycStatus;

	private Long totalGrounds;

	private Long activeGrounds;

	private Long inactiveGrounds;

	private Long pendingGrounds;

	private Long approvedGrounds;

	private Long rejectedGrounds;

	private Long totalBookings;

	private Double totalRevenue;
}