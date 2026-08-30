package com.app.hourbook.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.GroundRequestDto;
import com.app.hourbook.dto.GroundResponseDto;
import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.exception.ResourceNotFoundException;
import com.app.hourbook.model.Ground;
import com.app.hourbook.model.Vendor;
import com.app.hourbook.repository.GroundRepository;
import com.app.hourbook.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroundServiceImpl implements GroundService {

	private final GroundRepository groundRepository;

	private final VendorRepository vendorRepository;

	@Override
	public GroundResponseDto createGround(GroundRequestDto dto, MultipartFile[] images) throws IOException {

		
		Ground ground = new Ground();

		ground.setName(dto.getName());
		ground.setLocation(dto.getLocation());
		ground.setPricePerHour(dto.getPricePerHour());
		ground.setSports(dto.getSports());
		ground.setType(dto.getType());
		ground.setAmenities(dto.getAmenities());

		ground.setOpeningTime(dto.getOpeningTime());
		ground.setClosingTime(dto.getClosingTime());
		ground.setSlotDurationMinutes(
				dto.getSlotDurationMinutes() != null
						? dto.getSlotDurationMinutes()
						: 60);

		ground.setIsActive(true);
		ground.setStatus(GroundStatus.PENDING);

		// Vendor Mapping
		Vendor vendor = vendorRepository.findById(dto.getVendorId())
				.orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

		ground.setVendor(vendor);

		// Image Upload
		List<String> imageUrls = new ArrayList<>();

		if (images != null) {

			for (MultipartFile image : images) {

				if (!image.isEmpty()) {

					String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

					Path uploadPath = Paths.get("uploads");

					if (!Files.exists(uploadPath)) {
						Files.createDirectories(uploadPath);
					}

					Files.copy(image.getInputStream(), uploadPath.resolve(fileName),
							StandardCopyOption.REPLACE_EXISTING);

					imageUrls.add("/uploads/" + fileName);
				}
			}
		}

		ground.setImageUrls(imageUrls);

		Ground savedGround = groundRepository.save(ground);

		return mapToResponse(savedGround);
	}

	@Override
	public GroundResponseDto getGroundById(Integer groundId) {

		Ground ground = groundRepository.findById(groundId)
				.orElseThrow(() -> new ResourceNotFoundException("Ground not found"));
		System.out.println(ground.getImageUrls());
		return mapToResponse(ground);
	}

	@Override
	public List<GroundResponseDto> getAllGrounds() {

		return groundRepository.findAll().stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<GroundResponseDto> getGroundsByStatus(GroundStatus status) {

		return groundRepository.findByStatus(status).stream().map(this::mapToResponse).toList();
	}

	@Override
	public GroundResponseDto updateGround(Integer groundId, GroundRequestDto dto, MultipartFile[] images)
			throws IOException {

		Ground ground = groundRepository.findById(groundId)
				.orElseThrow(() -> new ResourceNotFoundException("Ground not found"));

		ground.setName(dto.getName());
		ground.setLocation(dto.getLocation());
		ground.setPricePerHour(dto.getPricePerHour());
		ground.setSports(dto.getSports());
		ground.setType(dto.getType());
		ground.setAmenities(dto.getAmenities());

		ground.setOpeningTime(dto.getOpeningTime());
		ground.setClosingTime(dto.getClosingTime());
		ground.setSlotDurationMinutes(
				dto.getSlotDurationMinutes() != null
						? dto.getSlotDurationMinutes()
						: 60);

		// Update Vendor
		Vendor vendor = vendorRepository.findById(dto.getVendorId())
				.orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

		ground.setVendor(vendor);

		// Update Images (only if new images are uploaded)
		if (images != null && images.length > 0 && !images[0].isEmpty()) {

			List<String> imageUrls = new ArrayList<>();

			for (MultipartFile image : images) {

				if (!image.isEmpty()) {

					String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

					Path uploadPath = Paths.get("uploads");

					if (!Files.exists(uploadPath)) {
						Files.createDirectories(uploadPath);
					}

					Files.copy(image.getInputStream(), uploadPath.resolve(fileName),
							StandardCopyOption.REPLACE_EXISTING);

					imageUrls.add("/uploads/" + fileName);
				}
			}

			ground.setImageUrls(imageUrls);

		} else {
			ground.setImageUrls(ground.getImageUrls());
		}
		// else: do nothing, keep existing images

		Ground updatedGround = groundRepository.save(ground);

		return mapToResponse(updatedGround);
	}

	@Override
	public String disableGround(Integer groundId) {

		Ground ground = groundRepository.findById(groundId)
				.orElseThrow(() -> new ResourceNotFoundException("Ground not found"));

		ground.setIsActive(false);

		groundRepository.save(ground);

		return "Ground disabled successfully.";
	}

	@Override
	public String enableGround(Integer groundId) {

		Ground ground = groundRepository.findById(groundId)
				.orElseThrow(() -> new ResourceNotFoundException("Ground not found"));

		ground.setIsActive(true);

		groundRepository.save(ground);

		return "Ground enabled successfully.";
	}

	@Override
	public String approveGround(Integer groundId) {

		Ground ground = groundRepository.findById(groundId)
				.orElseThrow(() -> new ResourceNotFoundException("Ground not found"));

		ground.setStatus(GroundStatus.APPROVED);

		groundRepository.save(ground);

		return "Ground approved successfully.";
	}

	@Override
	public String rejectGround(Integer groundId) {

		Ground ground = groundRepository.findById(groundId)
				.orElseThrow(() -> new ResourceNotFoundException("Ground not found"));

		ground.setStatus(GroundStatus.REJECTED);

		groundRepository.save(ground);

		return "Ground rejected successfully.";
	}

	@Override
	public String deleteGround(Integer groundId) {

		Ground ground = groundRepository.findById(groundId)
				.orElseThrow(() -> new ResourceNotFoundException("Ground not found"));

		groundRepository.delete(ground);

		return "Ground deleted successfully.";
	}

	private GroundResponseDto mapToResponse(Ground ground) {

		GroundResponseDto dto = new GroundResponseDto();

		dto.setId(ground.getId());
		dto.setName(ground.getName());
		dto.setLocation(ground.getLocation());
		dto.setSports(ground.getSports());
		dto.setPricePerHour(ground.getPricePerHour());
		dto.setAmenities(ground.getAmenities());
		dto.setImageUrls(ground.getImageUrls());
		dto.setType(ground.getType());
		dto.setIsActive(ground.getIsActive());
		dto.setStatus(ground.getStatus());

		dto.setOpeningTime(ground.getOpeningTime());
		dto.setClosingTime(ground.getClosingTime());
		dto.setSlotDurationMinutes(ground.getSlotDurationMinutes());

		if (ground.getVendor() != null) {
			dto.setVendorId(ground.getVendor().getId());
			dto.setVendorPhoneNumber(ground.getVendor().getPhoneNumber());
		}
		return dto;
	}
}