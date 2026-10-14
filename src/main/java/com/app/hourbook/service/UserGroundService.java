package com.app.hourbook.service;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.model.Ground;
import com.app.hourbook.repository.GroundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroundService {

    private final GroundRepository groundRepository;

    @Transactional(readOnly = true)
    public Page<Ground> getGrounds(
            int page,
            int size,
            String search) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id"));

        Page<Ground> groundPage;

        if (search != null && !search.isBlank()) {

            groundPage = groundRepository
                    .findByStatusAndIsActiveTrueAndNameContainingIgnoreCase(
                            GroundStatus.APPROVED,
                            search,
                            pageable);

        } else {

            groundPage = groundRepository
                    .findByStatusAndIsActiveTrue(
                            GroundStatus.APPROVED,
                            pageable);
        }

        // Initialize lazy-loaded fields before the transaction ends.
        groundPage.getContent().forEach(ground -> {
            Hibernate.initialize(ground.getImageUrls());
            Hibernate.initialize(ground.getAmenities());
        });

        return groundPage;
    }

    @Transactional(readOnly = true)
    public Ground getGround(Integer id) {

        Ground ground = groundRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ground not found"));

        Hibernate.initialize(ground.getImageUrls());
        Hibernate.initialize(ground.getAmenities());

        return ground;
    }
}