package com.app.hourbook.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.hourbook.model.Ground;
import com.app.hourbook.repository.GroundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorGroundService {

    private final GroundRepository groundRepository;

    public Page<Ground> getGrounds(Integer vendorId,
                                   int page,
                                   int size,
                                   String search){

        Pageable pageable =
                PageRequest.of(page,
                        size,
                        Sort.by(Sort.Direction.DESC,"id"));

        if(search==null || search.isBlank()){

            return groundRepository.findByVendor_Id(
                    vendorId,
                    pageable);

        }

        return groundRepository
                .findByVendor_IdAndNameContainingIgnoreCase(
                        vendorId,
                        search,
                        pageable);

    }

}