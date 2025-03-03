package com.clinitalPlatform.services;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MedecinImage;
import com.clinitalPlatform.repository.MedecinImageRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MedecinImageService {

    @Autowired
    private MedecinImageRepository medecinImageRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Value("${app.upload.dir:${user.home}/uploads/medecins}")
    private String uploadDir;

    //@Autowired
    //private AzureServices azureServices;

    private static final String MEDECINS_FOLDER = "medecins_images";

    /**
     * Récupérer toutes les images d'un médecin
     */
    public List<MedecinImage> getAllImagesByMedecinId(Long medecinId) {
        return medecinImageRepository.findByMedecinId(medecinId);
    }

    /**
     * Récupérer les images d'un médecin par type
     */
    public List<MedecinImage> getImagesByMedecinIdAndType(Long medecinId, String type) {
        return medecinImageRepository.findByMedecinIdAndType(medecinId, String.valueOf(type).toUpperCase());
    }

    /**
     * Récupérer l'image active d'un type spécifique pour un médecin
     */
    public Optional<MedecinImage> getActiveImageByMedecinIdAndType(Long medecinId, String type) {
        return medecinImageRepository.findActiveImageByMedecinIdAndType(medecinId, String.valueOf(type).toUpperCase());
    }

    /**
     * Récupérer une image spécifique
     */
    public Optional<MedecinImage> getImageById(Long imageId) {
        return medecinImageRepository.findById(imageId);
    }

    /**
     * Télécharger et créer une nouvelle image en utilisant Azure Blob Storage
     */
    @Transactional
    public MedecinImage uploadImage(Long medecinId, MultipartFile file, String type, String description) throws IOException {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'ID: " + medecinId));

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Créer le chemin de sous-dossier pour ce médecin
        String medecinFolder = MEDECINS_FOLDER + "/" + medecinId;

        // Télécharger le fichier sur Azure
        //String imageUrl = azureServices.upload(file, fileName, medecinFolder);

        // Normaliser le type
        String normalizedType = String.valueOf(type).toUpperCase();

        // Créer l'entrée dans la base de données
        MedecinImage medecinImage = new MedecinImage();
        //medecinImage.setImageUrl(imageUrl);
        medecinImage.setType(normalizedType);
        medecinImage.setDescription(description);
        medecinImage.setMedecin(medecin);

        // Si c'est une image de profil ou de couverture et qu'il n'y en a pas d'actives,
        // activer celle-ci par défaut
        if (("PROFILE".equals(normalizedType) || "COVER".equals(normalizedType)) &&
                !medecinImageRepository.findActiveImageByMedecinIdAndType(medecinId, normalizedType).isPresent()) {
            medecinImage.setActive(true);
        } else {
            medecinImage.setActive(false);
        }

        return medecinImageRepository.save(medecinImage);
    }


    /**
     * Mettre à jour les informations d'une image
     */
    @Transactional
    public MedecinImage updateImage(Long imageId, String description, boolean isActive) {
        MedecinImage image = medecinImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image non trouvée avec l'ID: " + imageId));

        if (description != null) {
            image.setDescription(description);
        }

        // Si on veut activer cette image
        if (isActive && !image.isActive()) {
            // Désactiver toutes les autres images du même type pour ce médecin
            List<MedecinImage> imagesOfSameType = medecinImageRepository.findByMedecinIdAndType(
                    image.getMedecin().getId(), image.getType());

            for (MedecinImage img : imagesOfSameType) {
                if (!img.getId().equals(imageId)) {
                    img.setActive(false);
                    medecinImageRepository.save(img);
                }
            }

            image.setActive(true);
        } else if (!isActive) {
            image.setActive(false);
        }

        return medecinImageRepository.save(image);
    }

    /**
     * Définir une image comme active et désactiver les autres du même type
     */
    @Transactional
    public MedecinImage setActiveImage(Long medecinId, Long imageId) {
        MedecinImage targetImage = medecinImageRepository.findByIdAndMedecinId(imageId, medecinId)
                .orElseThrow(() -> new RuntimeException("Image non trouvée pour ce médecin"));

        // Désactiver toutes les images du même type
        List<MedecinImage> images = medecinImageRepository.findByMedecinIdAndType(
                medecinId, targetImage.getType());

        for (MedecinImage img : images) {
            img.setActive(false);
            if (img.getId().equals(imageId)) {
                img.setActive(true);
            }
            medecinImageRepository.save(img);
        }

        return targetImage;
    }

    /**
     * Extraire le dossier à partir d'une URL Azure
     */
    private String extractFolderFromUrl(String imageUrl) {
        // Format attendu: https://[account].blob.core.windows.net/[container]/medecins/[medecinId]/[filename]
        String[] parts = imageUrl.split("/");
        // Récupérer la partie "medecins/[medecinId]"
        if (parts.length >= 2) {
            int medecinsIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("medecins".equals(parts[i])) {
                    medecinsIndex = i;
                    break;
                }
            }

            if (medecinsIndex >= 0 && parts.length > medecinsIndex + 1) {
                return parts[medecinsIndex] + "/" + parts[medecinsIndex + 1];
            }
        }
        return MEDECINS_FOLDER;
    }

    /**
     * Extraire le nom du fichier à partir d'une URL Azure
     * @param imageUrl URL complète du blob Azure
     * @return Le nom du fichier extrait de l'URL
     */
    private String extractFilenameFromUrl(String imageUrl) {
        // Vérifier si l'URL est valide
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }

        // Format attendu: https://[account].blob.core.windows.net/[container]/medecins_images/[medecinId]/[filename]
        // Récupérer la dernière partie de l'URL qui correspond au nom du fichier
        String[] parts = imageUrl.split("/");

        // Si l'URL est valide, le dernier élément devrait être le nom du fichier
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }

        // Si aucun nom de fichier n'a été trouvé, retourner une chaîne vide
        return "";
    }

    /**
     * Supprimer une image
     */
    @Transactional
    public void deleteImage(Long medecinId, Long imageId) throws IOException {
        MedecinImage image = medecinImageRepository.findByIdAndMedecinId(imageId, medecinId)
                .orElseThrow(() -> new RuntimeException("Image non trouvée pour ce médecin"));

        try {
            // Extraire le nom du fichier à partir de l'URL
            String filename = extractFilenameFromUrl(image.getImageUrl());
            String folder = extractFolderFromUrl(image.getImageUrl());

            // Supprimer le blob
            //azureServices.deleteBlob(folder + "/" + filename);
        } catch (Exception e) {
            // Ignorer les erreurs de suppression du fichier, mais les logger
            System.err.println("Erreur lors de la suppression du fichier dans Azure: " + e.getMessage());
        }

        // Supprimer l'entrée de la base de données
        medecinImageRepository.delete(image);

        // Si c'était une image active, essayer d'activer une autre image du même type si disponible
        if (image.isActive()) {
            List<MedecinImage> remainingImages = medecinImageRepository.findByMedecinIdAndType(
                    medecinId, image.getType());

            if (!remainingImages.isEmpty()) {
                MedecinImage newActiveImage = remainingImages.get(0);
                newActiveImage.setActive(true);
                medecinImageRepository.save(newActiveImage);
            }
        }
    }

    /**
     * Supprimer toutes les images d'un médecin
     */
    @Transactional
    public void deleteAllImagesByMedecinId(Long medecinId) throws IOException {
        List<MedecinImage> images = medecinImageRepository.findByMedecinId(medecinId);

        // Supprimer tous les fichiers dans Azure
        for (MedecinImage image : images) {
            try {
                String filename = extractFilenameFromUrl(image.getImageUrl());
                String folder = extractFolderFromUrl(image.getImageUrl());
                //azureServices.deleteBlob(folder + "/" + filename);
            } catch (Exception e) {
                // Ignorer les erreurs de suppression du fichier, mais les logger
                System.err.println("Erreur lors de la suppression du fichier dans Azure: " + e.getMessage());
            }
        }

        // Supprimer toutes les entrées de la base de données
        medecinImageRepository.deleteByMedecinId(medecinId);
    }

    /**
     * Synchroniser les champs hérités (photo_med et photo_couverture_med) avec les images actives
     */
    @Transactional
    public void synchronizeMedecinPhotos(Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'ID: " + medecinId));

        // Mise à jour de la photo de profil
        Optional<MedecinImage> profileImage = medecinImageRepository.findActiveImageByMedecinIdAndType(medecinId, "PROFILE");
        if (profileImage.isPresent()) {
            medecin.setPhoto_med(profileImage.get().getImageUrl());
        }

        // Mise à jour de la photo de couverture
        Optional<MedecinImage> coverImage = medecinImageRepository.findActiveImageByMedecinIdAndType(medecinId, "COVER");
        if (coverImage.isPresent()) {
            medecin.setPhoto_couverture_med(coverImage.get().getImageUrl());
        }

        medecinRepository.save(medecin);
    }
}
