package com.app.hourbook.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.hourbook.dto.admin.AdminLoginRequest;
import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.enums.Role;
import com.app.hourbook.model.AppAdmin;
import com.app.hourbook.model.AppUserDetails;
import com.app.hourbook.model.Ground;
import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.AdminRepository;
import com.app.hourbook.repository.AppUserDetailsRepository;
import com.app.hourbook.repository.GroundRepository;
import com.app.hourbook.repository.VendorDetailsRepository;
import com.app.hourbook.repository.VendorRepository;
import com.app.hourbook.service.AdminMvcService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminMvcController {

	private final AdminRepository adminRepository;
	private final AppUserDetailsRepository userRepository;
	private final VendorRepository vendorRepository;
	private final GroundRepository groundRepository;
	private final VendorDetailsRepository vendorDetailsRepository;


	private final PasswordEncoder passwordEncoder;
	private final AdminMvcService adminMvcService;

	@PostMapping("/users/status/{id}")
	public String changeUserStatus(@PathVariable Integer id) {

		AppUserDetails user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		user.setActive(!user.isActive());

		userRepository.save(user);

		return "redirect:/admin/users";
	}

	@GetMapping("/login")
	public String loginPage(Model model) {
		model.addAttribute("adminLoginRequest", new AdminLoginRequest());
		return "admin/login";
	}

	@PostMapping("/login")
	public String login(
	        @ModelAttribute AdminLoginRequest request,
	        Model model,
	        HttpSession session) {

	    AppAdmin admin = adminRepository
	            .findByEmail(request.getEmail())
	            .orElse(null);

	    if (admin == null ||
	            !passwordEncoder.matches(
	                    request.getPassword(),
	                    admin.getPassword())) {

	        model.addAttribute("error", "Invalid email or password");
	        return "admin/login";
	    }

	    if (!admin.isVerified()) {
	        model.addAttribute("error", "Admin account is not verified");
	        return "admin/login";
	    }

	    if (admin.getRole() != Role.ROLE_ADMIN) {
	        model.addAttribute("error", "Unauthorized access");
	        return "admin/login";
	    }

	    session.setAttribute("ADMIN", admin);

	    return "redirect:/admin/dashboard";
	}
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/admin/login";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {

	    model.addAttribute("usersCount", userRepository.count());

	    model.addAttribute("vendorsCount", vendorRepository.count());

	    model.addAttribute("groundsCount", groundRepository.count());

	    model.addAttribute("pendingGrounds",
	            groundRepository.countByStatus(GroundStatus.PENDING));

	    model.addAttribute("approvedGrounds",
	            groundRepository.countByStatus(GroundStatus.APPROVED));

	    model.addAttribute("rejectedGrounds",
	            groundRepository.countByStatus(GroundStatus.REJECTED));

	    return "admin/dashboard";
	}

	@GetMapping("/users")
	public String users(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(required = false) String search,
	        Model model) {

	    Page<AppUserDetails> userPage = 
	    		adminMvcService.getUsers(page, size,search);

	    int current = userPage.getNumber();
	    int total = userPage.getTotalPages();

	    // 5-page window centered on the current page, clamped to valid bounds
	    int start = Math.max(0, current - 2);
	    int end = Math.min(total - 1, current + 2);

	    // If we're near the start/end, shift the window so it still shows 5 pages
	    if (end - start < 4) {
	        if (start == 0) {
	            end = Math.min(total - 1, start + 4);
	        } else if (end == total - 1) {
	            start = Math.max(0, end - 4);
	        }
	    }

	    model.addAttribute("userPage", userPage);
	    model.addAttribute("users", userPage.getContent());
	    model.addAttribute("search", search);
	    model.addAttribute("startPage", start);
	    model.addAttribute("endPage", end);

	    return "admin/users";
	}


	@PostMapping("/vendors/status/{id}")
	public String changeVendorStatus(@PathVariable Integer id) {

		VendorDetails vendor = vendorDetailsRepository.findByVendorAuthId(id)
				.orElseThrow(() -> new RuntimeException("Vendor not found"));

		vendor.setActive(!vendor.isActive());

		vendorDetailsRepository.save(vendor);

		return "redirect:/admin/vendors";
	}

	@GetMapping("/vendors")
	public String vendors(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(required = false) String search,
	        Model model) {

	    Page<VendorDetails> vendorPage = adminMvcService.getVendors(page, size,search);

	    int current = vendorPage.getNumber();
	    int total = vendorPage.getTotalPages();

	    // 5-page window centered on the current page, clamped to valid bounds
	    int start = Math.max(0, current - 2);
	    int end = Math.min(total - 1, current + 2);

	    // If we're near the start/end, shift the window so it still shows 5 pages
	    if (end - start < 4) {
	        if (start == 0) {
	            end = Math.min(total - 1, start + 4);
	        } else if (end == total - 1) {
	            start = Math.max(0, end - 4);
	        }
	    }

	    model.addAttribute("vendorPage", vendorPage);
	    model.addAttribute("vendors", vendorPage.getContent());
	    model.addAttribute("search", search);
	    model.addAttribute("startPage", start);
	    model.addAttribute("endPage", end);

	    return "admin/vendors";
	}
	
	@GetMapping("/grounds")
	public String grounds(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(required = false) String status,
	        @RequestParam(required = false) String search,
	        Model model) {

	    Page<Ground> groundPage =
	            adminMvcService.getGrounds(page, size, status, search);

	    int current = groundPage.getNumber();
	    int total = groundPage.getTotalPages();

	    int start = Math.max(0, current - 5);
	    int end = Math.min(total - 1, current + 5);

	    if (end - start < 10) {
	        if (start == 0) {
	            end = Math.min(total - 1, start + 10);
	        } else if (end == total - 1) {
	            start = Math.max(0, end - 10);
	        }
	    }

	    model.addAttribute("groundPage", groundPage);
	    model.addAttribute("grounds", groundPage.getContent());

	    model.addAttribute("status", status);
	    model.addAttribute("search", search);

	    model.addAttribute("startPage", start);
	    model.addAttribute("endPage", end);

	    return "admin/grounds";
	}
	
	@PostMapping("/vendors/kyc/approve/{id}")
	public String approveKyc(@PathVariable Integer id){

	    adminMvcService.approveVendorKyc(id);

	    return "redirect:/admin/vendors";
	}
	
	@PostMapping("/vendors/kyc/reject/{id}")
	public String rejectKyc(@PathVariable Integer id){

	    adminMvcService.rejectVendorKyc(id);

	    return "redirect:/admin/vendors";
	}

	@GetMapping("/bookings")
	public String bookings() {
		return "admin/bookings";
	}

	
}