package com.app.hourbook.controller.vendor;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.hourbook.dto.VendorGroundRequestDto;
import com.app.hourbook.model.Ground;
import com.app.hourbook.service.VendorPanelGroundService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor/grounds")
@RequiredArgsConstructor
public class VendorPanelGroundController {

	private final VendorPanelGroundService vendorPanelGroundService;

	// =========================
	// Ground List
	// =========================

	@GetMapping
	public String grounds(

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size,

			@RequestParam(required = false) String search,

			HttpSession session,

			Model model) {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {

			return "redirect:/vendor/login";

		}

		Page<Ground> groundPage = vendorPanelGroundService.getVendorGrounds(vendorId, page, size, search);

		model.addAttribute("groundPage", groundPage);

		model.addAttribute("grounds", groundPage.getContent());

		model.addAttribute("search", search);

		return "vendor/grounds";
	}

	// =========================
	// Add Ground Page
	// =========================

	@GetMapping("/add")
	public String addGroundPage(

			HttpSession session,

			Model model) {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {

			return "redirect:/vendor/login";

		}

		VendorGroundRequestDto ground = new VendorGroundRequestDto();

		/*
		 * Version 1: Fixed 60-minute slots
		 */
		ground.setSlotDurationMinutes(60);

		model.addAttribute("ground", ground);

		return "vendor/add-ground";
	}

	// =========================
	// Save Ground
	// =========================

	@PostMapping("/add")
	public String saveGround(

			@Valid @ModelAttribute("ground") VendorGroundRequestDto request,

			BindingResult result,

			HttpSession session)

			throws IOException {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {

			return "redirect:/vendor/login";

		}

		if (result.hasErrors()) {

			return "vendor/add-ground";

		}

		vendorPanelGroundService.saveGround(vendorId, request);

		return "redirect:/vendor/grounds";
	}

	// =========================
	// View Ground
	// =========================

	@GetMapping("/view/{id}")
	public String viewGround(

			@PathVariable Integer id,

			HttpSession session,

			Model model) {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {

			return "redirect:/vendor/login";

		}

		model.addAttribute("ground", vendorPanelGroundService.getGround(id, vendorId));

		return "vendor/view-ground";
	}

	// =========================
	// Edit Ground Page
	// =========================

	@GetMapping("/edit/{id}")
	public String editGround(@PathVariable Integer id, HttpSession session, Model model) {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {
			return "redirect:/vendor/login";
		}

		Ground ground = vendorPanelGroundService.getGroundForEdit(id, vendorId);

		VendorGroundRequestDto request = convertToDto(ground);

		model.addAttribute("ground", request);

		return "vendor/edit-ground";
	}

	// =========================
	// Update Ground
	// =========================

	@PostMapping("/edit/{id}")
	public String updateGround(

			@PathVariable Integer id,

			@Valid @ModelAttribute("ground") VendorGroundRequestDto request,

			BindingResult result,

			HttpSession session)

			throws IOException {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {

			return "redirect:/vendor/login";

		}

		if (result.hasErrors()) {

			/*
			 * Existing image URLs are needed again because MultipartFile images cannot
			 * restore the old images automatically.
			 */
			Ground existingGround = vendorPanelGroundService.getGround(id, vendorId);

			request.setImageUrls(existingGround.getImageUrls());

			return "vendor/edit-ground";

		}

		vendorPanelGroundService.updateGround(id, vendorId, request);

		return "redirect:/vendor/grounds";
	}

	// =========================
	// Active / Inactive
	// =========================

	@PostMapping("/toggle/{id}")
	public String toggleGround(

			@PathVariable Integer id,

			HttpSession session) {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {

			return "redirect:/vendor/login";

		}

		vendorPanelGroundService.toggleGroundStatus(id, vendorId);

		return "redirect:/vendor/grounds";
	}

	// =========================
	// Delete Ground
	// =========================

	@PostMapping("/delete/{id}")
	public String deleteGround(

			@PathVariable Integer id,

			HttpSession session) {

		Integer vendorId = (Integer) session.getAttribute("vendorId");

		if (vendorId == null) {

			return "redirect:/vendor/login";

		}

		vendorPanelGroundService.deleteGround(id, vendorId);

		return "redirect:/vendor/grounds";
	}

	// =========================
	// Entity → DTO
	// =========================

	private VendorGroundRequestDto convertToDto(Ground ground) {

		VendorGroundRequestDto dto = new VendorGroundRequestDto();

		dto.setId(ground.getId());
		dto.setName(ground.getName());
		dto.setLocation(ground.getLocation());
		dto.setSports(ground.getSports());
		dto.setPricePerHour(ground.getPricePerHour());
		dto.setType(ground.getType());

		dto.setAmenities(ground.getAmenities() == null ? new ArrayList<>() : new ArrayList<>(ground.getAmenities()));

		dto.setImageUrls(ground.getImageUrls() == null ? new ArrayList<>() : new ArrayList<>(ground.getImageUrls()));

		dto.setOpeningTime(ground.getOpeningTime());
		dto.setClosingTime(ground.getClosingTime());

		dto.setSlotDurationMinutes(ground.getSlotDurationMinutes() != null ? ground.getSlotDurationMinutes() : 60);

		return dto;
	}
}