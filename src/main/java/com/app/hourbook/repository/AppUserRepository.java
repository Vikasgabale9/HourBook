package com.app.hourbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.hourbook.model.AppUser;



@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Integer>
{
  
	Optional<AppUser> findByPhoneNumber(String phoneNumber);
	boolean existsByPhoneNumber(String phoneNumber);
	
	
	
}
