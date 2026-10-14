package com.app.hourbook.service;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMvcService {

    private final AppUserDetailsRepository userRepository;
    private final GroundRepository groundRepository;
    private final VendorDetailsRepository vendorDetailsRepository;

    /**
     * Returns a paginated list of users with optional search filtering.
     */
    public Page<AppUserDetails> getUsers(
            int page,
            int size,
            String search) {

        Pageable pageable = createPageable(page, size);

        Specification<AppUserDetails> specification =
                UserSpecification.search(search);

        return userRepository.findAll(specification, pageable);
    }

    /**
     * Returns a paginated list of vendors with optional search filtering.
     */
    public Page<VendorDetails> getVendors(
            int page,
            int size,
            String search) {

        Pageable pageable = createPageable(page, size);

        Specification<VendorDetails> specification =
                VendorSpecification.search(search);

        return vendorDetailsRepository.findAll(specification, pageable);
    }

    /**
     * Approves a vendor's KYC.
     */
    @Transactional
    public void approveVendorKyc(Integer vendorId) {

        VendorDetails vendor = findVendorById(vendorId);

        vendor.setKycStatus(KycStatus.VERIFIED);

        vendorDetailsRepository.save(vendor);
    }

    /**
     * Rejects a vendor's KYC.
     */
    @Transactional
    public void rejectVendorKyc(Integer vendorId) {

        VendorDetails vendor = findVendorById(vendorId);

        vendor.setKycStatus(KycStatus.REJECTED);

        vendorDetailsRepository.save(vendor);
    }

    /**
     * Returns a paginated list of grounds with optional status
     * and search filters.
     *
     * Initializes lazy relationships used by the admin grounds page
     * before the Hibernate session closes.
     */
    @Transactional(readOnly = true)
    public Page<Ground> getGrounds(
            int page,
            int size,
            String status,
            String search) {

        Pageable pageable = createPageable(page, size);

        GroundStatus groundStatus = parseGroundStatus(status);

        Page<Ground> groundPage = groundRepository.findAll(
                GroundSpecification.filter(search, groundStatus),
                pageable
        );

        groundPage.getContent().forEach(ground -> {
            Hibernate.initialize(ground.getVendor());
            Hibernate.initialize(ground.getImageUrls());
            Hibernate.initialize(ground.getAmenities());
        });

        return groundPage;
    }

    /**
     * Creates a pageable request with consistent sorting.
     */
    private Pageable createPageable(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Page size must be greater than zero"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    /**
     * Finds a vendor or throws an exception when the vendor does not exist.
     */
    private VendorDetails findVendorById(Integer vendorId) {

        if (vendorId == null) {
            throw new IllegalArgumentException(
                    "Vendor ID cannot be null"
            );
        }

        return vendorDetailsRepository.findById(vendorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vendor not found with ID: " + vendorId
                        )
                );
    }

    /**
     * Converts the supplied status into a GroundStatus enum.
     * A blank or missing status means no status filter.
     */
    private GroundStatus parseGroundStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return GroundStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid ground status: " + status
            );
        }
    }
}