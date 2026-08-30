package com.app.hourbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.app.hourbook.model.AppUserDetails;

@Repository
public interface AppUserDetailsRepository extends JpaRepository<AppUserDetails, Integer> 
,JpaSpecificationExecutor<AppUserDetails>{

	Optional<AppUserDetails> findByEmail(String email);

    Optional<AppUserDetails> findByAppUserId(Integer appUserId);

    Optional<AppUserDetails> findByAppUserPhoneNumber(String phoneNumber);
    
    Optional<AppUserDetails> findByEmailOrPhoneNumber(
            String email,
            String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByAppUserId(Integer appUserId);
    
}
