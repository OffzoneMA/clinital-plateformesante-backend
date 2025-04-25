package com.clinitalPlatform.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Upload une image et retourne l'URL sécurisée
    public String uploadImage(MultipartFile file, String dossier) throws IOException {
        Map<String, Object> options = new HashMap<>();
        options.put("folder", dossier);
        options.put("resource_type", "auto");
        options.put("quality", "auto:best"); // Qualité optimale
        options.put("fetch_format", "auto"); // Format optimal
        options.put("flags", "preserve_transparency"); // Préserver la transparence si applicable

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
        return result.get("secure_url").toString();
    }

    // Supprime une image par son public_id (ex : dossier/nomImage)
    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de l'image Cloudinary", e);
        }
    }

    // Méthode utilitaire pour extraire le publicId de l'URL complète
    private String extractPublicIdFromUrl(String url) {
        String[] parts = url.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.split("\\.")[0]; // Enlève l'extension (ex: .jpg, .png)
    }


    // Récupère les infos complètes de l'image (optionnel)
    public Map<?, ?> getImageDetails(String publicId) throws Exception {
        return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
    }
}
