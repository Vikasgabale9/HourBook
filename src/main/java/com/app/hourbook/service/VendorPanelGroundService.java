
package com.app.hourbook.service;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.VendorGroundRequestDto;
import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.model.Ground;
import com.app.hourbook.model.Vendor;
import com.app.hourbook.repository.GroundRepository;
import com.app.hourbook.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorPanelGroundService {

    private final GroundRepository groundRepository;
    private final VendorRepository vendorRepository;
    private final CloudinaryImageService cloudinaryImageService;

    public void saveGround(
            Integer vendorId,
            VendorGroundRequestDto request) throws IOException {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        validateOperatingHours(
                request.getOpeningTime(),
                request.getClosingTime());

        Ground ground = new Ground();

        ground.setName(request.getName());
        ground.setLocation(request.getLocation());
        ground.setSports(request.getSports());
        ground.setPricePerHour(request.getPricePerHour());
        ground.setType(request.getType());

        ground.setAmenities(
                request.getAmenities() != null
                        ? new ArrayList<>(request.getAmenities())
                        : new ArrayList<>());

        ground.setOpeningTime(request.getOpeningTime());
        ground.setClosingTime(request.getClosingTime());
        ground.setSlotDurationMinutes(60);
        ground.setVendor(vendor);
        ground.setStatus(GroundStatus.PENDING);
        ground.setIsActive(true);

        List<String> imageUrls = new ArrayList<>();

        if (request.getImages() != null) {
            for (MultipartFile image : request.getImages()) {
                if (image != null && !image.isEmpty()) {
                    imageUrls.add(
                            cloudinaryImageService.uploadImage(image));
                }
            }
        }

        ground.setImageUrls(imageUrls);
        groundRepository.save(ground);
    }

    private void validateOperatingHours(
            LocalTime openingTime,
            LocalTime closingTime) {

        if (openingTime == null) {
            throw new IllegalArgumentException(
                    "Opening time is required");
        }

        if (closingTime == null) {
            throw new IllegalArgumentException(
                    "Closing time is required");
        }

        if (!openingTime.isBefore(closingTime)) {
            throw new IllegalArgumentException(
                    "Opening time must be before closing time");
        }
    }

    @Transactional(readOnly = true)
    public Page<Ground> getVendorGrounds(
            Integer vendorId,
            int page,
            int size,
            String search) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id"));

        Page<Ground> groundPage;

        if (search != null && !search.isBlank()) {
            groundPage =
                    groundRepository.findByVendor_IdAndNameContainingIgnoreCase(
                            vendorId, search, pageable);
        } else {
            groundPage =
                    groundRepository.findByVendor_Id(vendorId, pageable);
        }

        // Initialize lazy collections before the transaction ends.
        groundPage.getContent().forEach(ground -> {
            Hibernate.initialize(ground.getImageUrls());
            Hibernate.initialize(ground.getAmenities());
        });

        return groundPage;
    }

    @Transactional(readOnly = true)
    public Ground getGround(
            Integer groundId,
            Integer vendorId) {

        Ground ground = groundRepository.findWithAmenitiesById(groundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ground not found"));

        if (ground.getVendor() == null
                || !ground.getVendor().getId().equals(vendorId)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this ground");
        }

        Hibernate.initialize(ground.getAmenities());
        Hibernate.initialize(ground.getImageUrls());

        return ground;
    }

    @Transactional(readOnly = true)
    public Ground getGroundForEdit(
            Integer groundId,
            Integer vendorId) {

        return getGround(groundId, vendorId);
    }

    @Transactional
    public void updateGround(
            Integer groundId,
            Integer vendorId,
            VendorGroundRequestDto request) throws IOException {

        Ground ground = getGround(groundId, vendorId);

        validateOperatingHours(
                request.getOpeningTime(),
                request.getClosingTime());

        ground.setName(request.getName());
        ground.setLocation(request.getLocation());
        ground.setSports(request.getSports());
        ground.setPricePerHour(request.getPricePerHour());
        ground.setType(request.getType());

        ground.setAmenities(
                request.getAmenities() != null
                        ? new ArrayList<>(request.getAmenities())
                        : new ArrayList<>());

        ground.setOpeningTime(request.getOpeningTime());
        ground.setClosingTime(request.getClosingTime());
        ground.setSlotDurationMinutes(60);

        if (request.getImages() != null
                && !request.getImages().isEmpty()) {

            List<String> imageUrls = new ArrayList<>();

            for (MultipartFile image : request.getImages()) {
                if (image != null && !image.isEmpty()) {
                    imageUrls.add(
                            cloudinaryImageService.uploadImage(image));
                }
            }

            // Replace images only if new images were uploaded.
            if (!imageUrls.isEmpty()) {
                ground.setImageUrls(imageUrls);
            }
        }

        // The entity is managed within the transaction.
        groundRepository.save(ground);
    }

    public void deleteGround(
            Integer groundId,
            Integer vendorId) {

        Ground ground = getGround(groundId, vendorId);
        groundRepository.delete(ground);
    }

    public void toggleGroundStatus(
            Integer groundId,
            Integer vendorId) {

        Ground ground = getGround(groundId, vendorId);
        ground.setIsActive(!ground.getIsActive());
        groundRepository.save(ground);
    }
}
