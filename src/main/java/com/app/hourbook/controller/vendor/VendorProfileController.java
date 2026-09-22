package com.app.hourbook.controller.vendor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorProfileController {

    private final VendorDetailsRepository vendorDetailsRepository;

    // =========================
    // VIEW PROFILE
    // =========================
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        Integer vendorId = (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        VendorDetails vendor = vendorDetailsRepository
                .findByVendorAuth_Id(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor profile not found"));

        model.addAttribute("vendor", vendor);

        return "vendor/profile";
    }

    // =========================
    // EDIT PROFILE PAGE
    // =========================
    @GetMapping("/profile/edit")
    public String editProfile(HttpSession session, Model model) {

        Integer vendorId = (Integer) session.getAttribute("vendorId");

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

        Integer vendorId = (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        try {

            VendorDetails vendor = vendorDetailsRepository
                    .findByVendorAuth_Id(vendorId)
                    .orElseThrow(() ->
                            new RuntimeException("Vendor profile not found"));

            // Update name
            vendor.setFullName(fullName);

            // Update email
            vendor.setEmail(email);

            // =========================
            // PROFILE IMAGE
            // =========================
            if (profileImage != null && !profileImage.isEmpty()) {

                // Maximum 5 MB
                if (profileImage.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute(
                            "error",
                            "Profile image must not exceed 5 MB"
                    );

                    return "redirect:/vendor/profile/edit";
                }

                // Allowed image types
                String contentType = profileImage.getContentType();

                if (contentType == null ||
                        !(contentType.equals("image/jpeg")
                        || contentType.equals("image/jpg")
                        || contentType.equals("image/png"))) {

                    redirectAttributes.addFlashAttribute(
                            "error",
                            "Only JPG, JPEG and PNG images are allowed"
                    );

                    return "redirect:/vendor/profile/edit";
                }

                // Upload directory
                Path uploadDir = Paths.get(
                        "uploads/vendor/profile"
                );

                Files.createDirectories(uploadDir);

                // Generate unique filename
                String originalFileName =
                        profileImage.getOriginalFilename();

                String extension = "";

                if (originalFileName != null &&
                        originalFileName.contains(".")) {

                    extension = originalFileName.substring(
                            originalFileName.lastIndexOf(".")
                    );
                }

                String fileName =
                        UUID.randomUUID() + extension;

                Path filePath =
                        uploadDir.resolve(fileName);

                // Save file
                Files.copy(
                        profileImage.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                // Store URL/path in database
                String imagePath =
                        "/uploads/vendor/profile/" + fileName;

                vendor.setProfileImage(imagePath);
            }

            // Save vendor
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
                    "Profile updated successfully"
            );

            return "redirect:/vendor/profile";

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to upload profile image"
            );

            return "redirect:/vendor/profile/edit";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/vendor/profile/edit";
        }
    }
}