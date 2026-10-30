
package com.app.hourbook.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryImageService {

    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    // Existing ground/profile image upload.
    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, "hourbook/grounds");
    }

    public String uploadImage(
            MultipartFile file,
            String folder) throws IOException {

        validateImage(file);

        if (folder == null || folder.isBlank()) {
            throw new IllegalArgumentException(
                    "Cloudinary folder is required.");
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"
                )
        );

        Object secureUrl = result.get("secure_url");

        if (secureUrl == null) {
            throw new IOException(
                    "Cloudinary did not return an image URL.");
        }

        return secureUrl.toString();
    }

    // New method: upload KYC as an authenticated Cloudinary asset.
    // Returns public_id, NOT a public URL.
    public String uploadKycDocument(
            MultipartFile file,
            String documentType) throws IOException {

        validateImage(file);

        String folder;

        if ("AADHAAR".equalsIgnoreCase(documentType)) {
            folder = "hourbook/vendors/kyc/aadhaar";
        } else if ("PAN".equalsIgnoreCase(documentType)) {
            folder = "hourbook/vendors/kyc/pan";
        } else {
            throw new IllegalArgumentException(
                    "Invalid KYC document type.");
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image",
                        "type", "authenticated",
                        "unique_filename", true,
                        "overwrite", false
                )
        );

        Object publicId = result.get("public_id");

        if (publicId == null) {
            throw new IOException(
                    "Cloudinary did not return a KYC document ID.");
        }

        return publicId.toString();
    }

    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select an image.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size must not exceed 5 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are allowed.");
        }
    }
}
