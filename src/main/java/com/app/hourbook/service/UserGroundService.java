package com.app.hourbook.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.model.Ground;
import com.app.hourbook.repository.GroundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroundService {

    private final GroundRepository groundRepository;

    public Page<Ground> getGrounds(

            int page,

            int size,

            String search) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "id"));

        if (search != null && !search.isBlank()) {

            return groundRepository
                    .findByStatusAndIsActiveTrueAndNameContainingIgnoreCase(
                            GroundStatus.APPROVED,
                            search,
                            pageable);
        }

        return groundRepository
                .findByStatusAndIsActiveTrue(
                        GroundStatus.APPROVED,
                        pageable);
    }

    public Ground getGround(Integer id) {

        return groundRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ground not found"));
    }

}