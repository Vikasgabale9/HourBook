package com.app.hourbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.app.hourbook.model.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {

	Optional<Vendor> findByPhoneNumber(String phoneNumber);
	boolean existsByPhoneNumber(String phoneNumber);
	
}
