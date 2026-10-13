package com.app.hourbook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.model.Ground;

import jakarta.persistence.LockModeType;

@Repository
public interface GroundRepository
        extends JpaRepository<Ground, Integer>,
        JpaSpecificationExecutor<Ground> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT g FROM Ground g WHERE g.id = :groundId")
	Optional<Ground> findByIdForUpdate(
	        @Param("groundId") Integer groundId
	);
    // =========================
    // Admin Ground Queries
    // =========================

    List<Ground> findByStatus(GroundStatus status);

    long countByStatus(GroundStatus status);

    Page<Ground> findByStatus(
            GroundStatus status,
            Pageable pageable
    );


    // =========================
    // Vendor Dashboard Counts
    // =========================

    long countByVendor_Id(Integer vendorId);

    long countByVendor_IdAndIsActiveTrue(Integer vendorId);

    long countByVendor_IdAndIsActiveFalse(Integer vendorId);

    long countByVendor_IdAndStatus(
            Integer vendorId,
            GroundStatus status
    );


    // =========================
    // Vendor Ground Queries
    // =========================

    List<Ground> findByVendor_Id(Integer vendorId);

    Page<Ground> findByVendor_Id(
            Integer vendorId,
            Pageable pageable
    );

    List<Ground> findByVendor_IdAndIsActiveTrue(
            Integer vendorId
    );

    List<Ground> findByVendor_IdAndStatus(
            Integer vendorId,
            GroundStatus status
    );

    Page<Ground> findByVendor_IdAndNameContainingIgnoreCase(
            Integer vendorId,
            String name,
            Pageable pageable
    );


    // =========================
    // User Browse Grounds
    // =========================

    Page<Ground> findByStatusAndIsActiveTrue(
            GroundStatus status,
            Pageable pageable
    );

    Page<Ground>
    findByStatusAndIsActiveTrueAndNameContainingIgnoreCase(
            GroundStatus status,
            String name,
            Pageable pageable
    );

    List<Ground> findByStatusAndIsActiveTrue(
            GroundStatus status
    );


    // =========================
    // User Booking
    // =========================

    Optional<Ground> findByIdAndStatusAndIsActiveTrue(
            Integer id,
            GroundStatus status
    );

}