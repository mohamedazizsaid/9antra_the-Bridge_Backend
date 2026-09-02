package com._antra.the_bridge.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload an avatar image to Cloudinary under the_bridge/avatars/ folder.
     * Returns the secure HTTPS URL of the uploaded image.
     */
    @SuppressWarnings("unchecked")
    public String uploadAvatar(MultipartFile file) {
        try {
            String publicId = "the_bridge/avatars/" + UUID.randomUUID();
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id",      publicId,
                    "overwrite",      true,
                    "resource_type",  "image",
                    "transformation", "c_fill,w_300,h_300,g_face,q_auto,f_auto"
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Échec de l'upload de l'avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Upload a stage PDF document to Cloudinary under the_bridge/stage-docs/ folder.
     * Returns the secure HTTPS URL of the uploaded document.
     */
    @SuppressWarnings("unchecked")
    public String uploadStagePdf(MultipartFile file, String prefix) {
        try {
            String publicId = "the_bridge/stage-docs/" + prefix + "-" + UUID.randomUUID();
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id",     publicId,
                    "overwrite",     true,
                    "resource_type", "raw"
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Échec de l'upload du document PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Upload raw PDF bytes (e.g. generated in-memory with PDFBox) to Cloudinary.
     * Returns the secure HTTPS URL of the uploaded document.
     */
    @SuppressWarnings("unchecked")
    public String uploadPdfBytes(byte[] pdfBytes, String prefix) {
        try {
            String publicId = "the_bridge/attestations/" + prefix + "-" + UUID.randomUUID();
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id",     publicId,
                    "overwrite",     true,
                    "resource_type", "raw"
            );
            Map<String, Object> result = cloudinary.uploader().upload(pdfBytes, options);
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Échec de l'upload du PDF d'attestation: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an image from Cloudinary by its URL public ID.
     */
    public void deleteByUrl(String secureUrl) {
        try {
            // Extract public_id from URL: .../the_bridge/avatars/<uuid>.<ext>
            String[] parts = secureUrl.split("/upload/");
            if (parts.length < 2) return;
            String withVersion = parts[1];
            // Remove version prefix like v1234567890/
            String publicId = withVersion.replaceFirst("v\\d+/", "");
            // Remove extension
            int dotIdx = publicId.lastIndexOf('.');
            if (dotIdx > 0) publicId = publicId.substring(0, dotIdx);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException ignored) {
        }
    }
}
