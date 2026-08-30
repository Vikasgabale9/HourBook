package com.app.hourbook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.hourbook.dto.VendorDashboardResponseDto;
import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.GroundRepository;
import com.app.hourbook.repository.VendorDetailsRepository;

@Service
public class VendorDashboardService {

    @Autowired
    private VendorDetailsRepository vendorDetailsRepository;

    @Autowired
    private GroundRepository groundRepository;

    public VendorDashboardResponseDto getDashboard(Integer vendorId) {

        VendorDetails vendor = vendorDetailsRepository
                .findByVendorAuthId(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        VendorDashboardResponseDto dto = new VendorDashboardResponseDto();

        // ==========================
        // Vendor Information
        // ==========================

        dto.setVendorId(vendor.getVendorAuth().getId());
        dto.setVendorName(vendor.getFullName());
        dto.setVendorEmail(vendor.getEmail());
        dto.setPhoneNumber(vendor.getPhoneNumber());

        dto.setEmailVerified(vendor.isEmailVerified());
        dto.setPhoneVerified(vendor.getVendorAuth().isVerified());

        dto.setVendorActive(vendor.isActive());

        // Until KYC module is integrated
        dto.setKycStatus(vendor.getKycStatus());

        // ==========================
        // Ground Statistics
        // ==========================

        dto.setTotalGrounds(
                groundRepository.countByVendor_Id(vendorId));

        dto.setActiveGrounds(
                groundRepository.countByVendor_IdAndIsActiveTrue(vendorId));

        dto.setInactiveGrounds(
                groundRepository.countByVendor_IdAndIsActiveFalse(vendorId));

        dto.setPendingGrounds(
                groundRepository.countByVendor_IdAndStatus(
                        vendorId,
                        GroundStatus.PENDING));

        dto.setApprovedGrounds(
                groundRepository.countByVendor_IdAndStatus(
                        vendorId,
                        GroundStatus.APPROVED));

        dto.setRejectedGrounds(
                groundRepository.countByVendor_IdAndStatus(
                        vendorId,
                        GroundStatus.REJECTED));

        // ==========================
        // Booking Module
        // ==========================

        dto.setTotalBookings(0l);

        // ==========================
        // Revenue Module
        // ==========================

        dto.setTotalRevenue(0.0);

        return dto;
    }
}
