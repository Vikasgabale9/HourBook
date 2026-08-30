package com.app.hourbook.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.enums.KycStatus;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.model.Ground;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.AppUserDetailsRepository;
import com.app.hourbook.repository.GroundRepository;
import com.app.hourbook.repository.VendorDetailsRepository;
import com.app.hourbook.specification.GroundSpecification;
import com.app.hourbook.specification.UserSpecification;
import com.app.hourbook.specification.VendorSpecification;

@Service
public class AdminMvcService {

	private final AppUserDetailsRepository userRepository;
	private final GroundRepository groundRepository;
	private final VendorDetailsRepository vendorDetailsRepository;

	
	public AdminMvcService(AppUserDetailsRepository userRepository, GroundRepository groundRepository,
			VendorDetailsRepository vendorDetailsRepository) {
		super();
		this.userRepository = userRepository;
		this.groundRepository = groundRepository;
		this.vendorDetailsRepository = vendorDetailsRepository;
	}

	public Page<AppUserDetails> getUsers(int page, int size, String search) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
		Specification<AppUserDetails> specification = UserSpecification.search(search);

		return userRepository.findAll(specification, pageable);
	}

	public Page<VendorDetails> getVendors(int page, int size, String search) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
		Specification<VendorDetails> specification = VendorSpecification.search(search);

		return vendorDetailsRepository.findAll(specification, pageable);
	}

	public void approveVendorKyc(Integer vendorId){

	    VendorDetails vendor =
	            vendorDetailsRepository.findById(vendorId)
	            .orElseThrow(() -> new RuntimeException("Vendor not found"));

	    vendor.setKycStatus(KycStatus.VERIFIED);

	    vendorDetailsRepository.save(vendor);

	}
	public void rejectVendorKyc(Integer vendorId){

	    VendorDetails vendor =
	            vendorDetailsRepository.findById(vendorId)
	            .orElseThrow(() -> new RuntimeException("Vendor not found"));

	    vendor.setKycStatus(KycStatus.REJECTED);

	    vendorDetailsRepository.save(vendor);

	}
	
	public Page<Ground> getGrounds(int page, int size, String status, String search) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

		GroundStatus groundStatus = null;

		if (status != null && !status.isBlank()) {
			try {
				groundStatus = GroundStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				groundStatus = null;
			}
		}

		return groundRepository.findAll(GroundSpecification.filter(search, groundStatus), pageable);
	}
}
