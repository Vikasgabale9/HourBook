package com.app.hourbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.hourbook.model.AppAdmin;

@Repository
public interface AdminRepository extends JpaRepository<AppAdmin, Long>{

	 // Find methods
    Optional<AppAdmin> findByUsername(String username);

    Optional<AppAdmin> findByEmail(String email);

    Optional<AppAdmin> findByUsernameOrEmail(String username, String email);

    // Exists methods
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameOrEmail(String username, String email);

}
