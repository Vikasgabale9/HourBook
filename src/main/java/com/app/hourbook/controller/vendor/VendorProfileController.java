
package com.app.hourbook.controller.vendor;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.hourbook.model.VendorDetails;
import com.app.hourbook.repository.VendorDetailsRepository;
import com.app.hourbook.service.CloudinaryImageService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorProfileController {

    private final VendorDetailsRepository vendorDetailsRepository;
    private final CloudinaryImageService cloudinaryImageService;

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    // =========================
    // VIEW PROFILE
    // =========================

    @GetMapping("/profile")
    public String profile(
            HttpSession session,
            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorDetails vendor = vendorDetailsRepository
                .findByVendorAuth_Id(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor profile not found"));

        model.addAttribute("vendor", vendor);

        // Keep session synchronized with database.
        session.setAttribute("vendorName", vendor.getFullName());
        session.setAttribute("vendorEmail", vendor.getEmail());
        session.setAttribute("profileImage", vendor.getProfileImage());

        return "vendor/profile";
    }

    // =========================
    // EDIT PROFILE PAGE
    // =========================

    @GetMapping("/profile/edit")
    public String editProfile(
            HttpSession session,
            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorDetails vendor = vendorDetailsRepository
                .findByVendorAuth_Id(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor profile not found"));

        model.addAttribute("vendor", vendor);

        return "vendor/edit-profile";
    }

    // =========================
    // UPDATE PROFILE
    // =========================

    @PostMapping("/profile/update")
    public String updateProfile(
            HttpSession session,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) MultipartFile profileImage,
            RedirectAttributes redirectAttributes) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        try {

            VendorDetails vendor = vendorDetailsRepository
                    .findByVendorAuth_Id(vendorId)
                    .orElseThrow(() ->
                            new RuntimeException("Vendor profile not found"));

            // Update basic details.
            vendor.setFullName(fullName);
            vendor.setEmail(email);

            // =========================
            // CLOUDINARY PROFILE IMAGE
            // =========================

            if (profileImage != null && !profileImage.isEmpty()) {

                if (profileImage.getSize() > MAX_IMAGE_SIZE) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "Profile image must not exceed 5 MB."
                    );

                    return "redirect:/vendor/profile/edit";
                }

                String contentType = profileImage.getContentType();

                if (contentType == null ||
                        !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "Only JPG, PNG and WEBP images are allowed."
                    );

                    return "redirect:/vendor/profile/edit";
                }

                // Upload to Cloudinary.
                String imageUrl = cloudinaryImageService.uploadImage(
                        profileImage,
                        "hourbook/vendors/profiles"
                );

                // Save Cloudinary URL in database.
                vendor.setProfileImage(imageUrl);
            }

            // Save vendor details.
            VendorDetails updatedVendor =
                    vendorDetailsRepository.save(vendor);

            // =========================
            // UPDATE SESSION
            // =========================

            session.setAttribute(
                    "vendorName",
                    updatedVendor.getFullName()
            );

            session.setAttribute(
                    "vendorEmail",
                    updatedVendor.getEmail()
            );

            session.setAttribute(
                    "profileImage",
                    updatedVendor.getProfileImage()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profile updated successfully."
            );

            return "redirect:/vendor/profile";

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Cloudinary image upload failed. Please try again."
            );

            return "redirect:/vendor/profile/edit";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Profile update failed: " + e.getMessage()
            );

            return "redirect:/vendor/profile/edit";
        }
    }
}
