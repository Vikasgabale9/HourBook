package com.app.hourbook.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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


    public void saveGround(Integer vendorId,
                           VendorGroundRequestDto request) throws IOException {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        validateOperatingHours(
                request.getOpeningTime(),
                request.getClosingTime()
        );

        Ground ground = new Ground();

        ground.setName(request.getName());

        ground.setLocation(request.getLocation());

        ground.setSports(request.getSports());

        ground.setPricePerHour(request.getPricePerHour());

        ground.setType(request.getType());

        ground.setAmenities(request.getAmenities());

        /*
         * Ground operating hours
         */
        ground.setOpeningTime(request.getOpeningTime());

        ground.setClosingTime(request.getClosingTime());

        /*
         * Version 1 uses fixed 60-minute slots.
         */
        ground.setSlotDurationMinutes(60);

        ground.setVendor(vendor);

        ground.setStatus(GroundStatus.PENDING);

        ground.setIsActive(true);

        List<String> imageUrls = new ArrayList<>();

        if (request.getImages() != null) {

            for (MultipartFile image : request.getImages()) {

                if (!image.isEmpty()) {

                    imageUrls.add(uploadImage(image));

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
                    "Opening time is required"
            );

        }

        if (closingTime == null) {

            throw new IllegalArgumentException(
                    "Closing time is required"
            );

        }

        if (!openingTime.isBefore(closingTime)) {

            throw new IllegalArgumentException(
                    "Opening time must be before closing time"
            );

        }

    }


    private String uploadImage(MultipartFile file) throws IOException {

        String uploadDir = "uploads/grounds/";

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {

            Files.createDirectories(uploadPath);

        }

        String originalFileName = file.getOriginalFilename();

        String fileName =
                UUID.randomUUID()
                        + "_"
                        + (originalFileName != null
                        ? originalFileName
                        : "image");

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "/" + uploadDir + fileName;
    }


    public Page<Ground> getVendorGrounds(
            Integer vendorId,
            int page,
            int size,
            String search) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        if (search != null && !search.isBlank()) {

            return groundRepository
                    .findByVendor_IdAndNameContainingIgnoreCase(
                            vendorId,
                            search,
                            pageable
                    );

        }

        return groundRepository.findByVendor_Id(
                vendorId,
                pageable
        );
    }


    public Ground getGround(
            Integer groundId,
            Integer vendorId) {

        Ground ground = groundRepository
                .findById(groundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ground not found"
                        ));

        if (!ground.getVendor()
                .getId()
                .equals(vendorId)) {

            throw new RuntimeException(
                    "Unauthorized access"
            );

        }

        return ground;
    }


    public Ground getGroundForEdit(
            Integer groundId,
            Integer vendorId) {

        return getGround(groundId, vendorId);
    }


    public void updateGround(
            Integer groundId,
            Integer vendorId,
            VendorGroundRequestDto request)
            throws IOException {

        Ground ground = getGround(
                groundId,
                vendorId
        );

        validateOperatingHours(
                request.getOpeningTime(),
                request.getClosingTime()
        );

        ground.setName(request.getName());

        ground.setLocation(request.getLocation());

        ground.setSports(request.getSports());

        ground.setPricePerHour(request.getPricePerHour());

        ground.setType(request.getType());

        ground.setAmenities(request.getAmenities());

        /*
         * Update operating hours
         */
        ground.setOpeningTime(
                request.getOpeningTime()
        );

        ground.setClosingTime(
                request.getClosingTime()
        );

        /*
         * Fixed to 60 minutes in Version 1.
         */
        ground.setSlotDurationMinutes(60);

        if (request.getImages() != null
                && !request.getImages().isEmpty()) {

            List<String> imageUrls =
                    new ArrayList<>();

            for (MultipartFile image
                    : request.getImages()) {

                if (!image.isEmpty()) {

                    imageUrls.add(
                            uploadImage(image)
                    );

                }

            }

            if (!imageUrls.isEmpty()) {

                ground.setImageUrls(imageUrls);

            }

        }

        groundRepository.save(ground);
    }


    public void deleteGround(
            Integer groundId,
            Integer vendorId) {

        Ground ground = getGround(
                groundId,
                vendorId
        );

        groundRepository.delete(ground);
    }


    public void toggleGroundStatus(
            Integer groundId,
            Integer vendorId) {

        Ground ground = getGround(
                groundId,
                vendorId
        );

        ground.setIsActive(
                !ground.getIsActive()
        );

        groundRepository.save(ground);
    }

}