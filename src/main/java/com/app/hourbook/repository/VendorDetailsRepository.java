package com.app.hourbook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.app.hourbook.enums.KycStatus;
import com.app.hourbook.model.VendorDetails;

@Repository
public interface VendorDetailsRepository extends JpaRepository<VendorDetails, Integer> ,JpaSpecificationExecutor<VendorDetails>{

	Optional<VendorDetails> findByEmail(
            String email);
	
	Optional<VendorDetails> findByPhoneNumberOrEmail(
	        String phoneNumber,
	        String email);

    Optional<VendorDetails> findByVendorAuthId(
            Integer vendorId);

    Optional<VendorDetails> findByVendorAuthPhoneNumber(
            String phoneNumber);

    List<VendorDetails> findByKycStatus(KycStatus kycStatus);
            
    boolean existsByEmail(
            String email);

    boolean existsByVendorAuthId(
            Integer vendorId);
    

    Optional<VendorDetails> findByVendorAuth_Id(Integer vendorId);
    
}
