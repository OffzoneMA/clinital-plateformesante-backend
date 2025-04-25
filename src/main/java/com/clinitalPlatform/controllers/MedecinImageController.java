package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.MedecinImageDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MedecinImage;
import com.clinitalPlatform.repository.MedecinImageRepository;
import com.clinitalPlatform.services.CloudinaryService;
import com.clinitalPlatform.services.MedecinImageService;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.util.GlobalVariables;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medecin-images")
@CrossOrigin(origins = "*")
public class MedecinImageController {

    @Autowired
    private MedecinImageService medecinImageService;
    @Autowired
    private GlobalVariables globalVariables;
    @Autowired
    private MedecinService medecinService;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private MedecinImageRepository medecinImageRepository;

    /**
     * Récupérer toutes les images d'un médecin
     */
    @GetMapping("/{medecinId}")
    public ResponseEntity<?> getAllImages(@PathVariable Long medecinId) {
        try {
           List<MedecinImage> images = medecinImageService.getAllImagesByMedecinId(medecinId);
            if (images.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
            // Convertir les images en DTOs
            List<MedecinImageDTO> imageDTOs = images.stream()
                .map(image -> {
                    MedecinImageDTO dto = new MedecinImageDTO(); // Créez l'objet sans passer par le constructeur
                    dto.setId(image.getId());
                    dto.setImageUrl(image.getImageUrl());
                    dto.setDescription(image.getDescription());
                    dto.setType(image.getType());
                    dto.setActive(image.isActive());
                    return dto; // Retourne l'objet initialisé avec les setters
                })
                .toList();
            return ResponseEntity.ok(imageDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Récupérer toutes les images du médecin connecté
     */
    @GetMapping("/connected-med")
    public ResponseEntity<?> getConnectedMedecinImages() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin != null) {
                List<MedecinImage> images = medecinImageService.getAllImagesByMedecinId(medecin.getId());
                if (images.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
                }

                List<MedecinImageDTO> imageDTOs = images.stream()
                    .map(image -> {
                        MedecinImageDTO dto = new MedecinImageDTO(); // Créez l'objet sans passer par le constructeur
                        dto.setId(image.getId());
                        dto.setImageUrl(image.getImageUrl());
                        dto.setDescription(image.getDescription());
                        dto.setType(image.getType());
                        dto.setActive(image.isActive());
                        return dto; // Retourne l'objet initialisé avec les setters
                    })
                    .toList();

                return ResponseEntity.ok(imageDTOs);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Récupérer toutes les images d'un médecin par type
     */
    @GetMapping("/{medecinId}/type/{type}")
    public ResponseEntity<List<MedecinImage>> getImagesByType(
            @PathVariable Long medecinId,
            @PathVariable String type) {
        return ResponseEntity.ok(medecinImageService.getImagesByMedecinIdAndType(medecinId, type));
    }

    /**
     * Récupérer l'image active d'un type spécifique
     */
    @GetMapping("/{medecinId}/active/{type}")
    public ResponseEntity<?> getActiveImageByType(
            @PathVariable Long medecinId,
            @PathVariable String type) {
        return medecinImageService.getActiveImageByMedecinIdAndType(medecinId, type)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer une image spécifique
     */
    @GetMapping("/image/{imageId}")
    public ResponseEntity<?> getImageById(@PathVariable Long imageId) {
        return medecinImageService.getImageById(imageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Télécharger une nouvelle image
     */
    @PostMapping("/upload/connected")
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("description") String description,
            @RequestParam("type") String type) {

        try {
            Long userId = globalVariables.getConnectedUser().getId();
            // Vérification de l'existence du médecin
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé.");
            }

            // Upload de l'image sur Cloudinary
            String imageUrl = cloudinaryService.uploadImage(image, "medecin_images");

            // Création d'un objet MedecinImage et sauvegarde
            MedecinImage medecinImage = new MedecinImage();
            medecinImage.setImageUrl(imageUrl);
            medecinImage.setDescription(image.getOriginalFilename() + " - " + description + " - " + type + " - " + image.getSize());
            medecinImage.setType(type);
            medecinImage.setMedecin(medecin);
            medecinImage.setActive(true); // Par défaut, l'image est active
            medecinImageRepository.save(medecinImage);

            return ResponseEntity.ok("Image uploadée avec succès.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erreur lors de l'upload de l'image.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Endpoint pour uploader plusieurs images
    @PostMapping("/upload-multi/connected")
    public ResponseEntity<?> uploadImages(
            @RequestParam("images") MultipartFile[] images,  // Tableau de fichiers
            @RequestParam("description") String description,
            @RequestParam("type") String type) {

        try {
            // Récupérer l'ID de l'utilisateur connecté
            Long userId = globalVariables.getConnectedUser().getId();

            // Vérification de l'existence du médecin
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé.");
            }

            // Parcours des images pour les uploader une par une
            for (MultipartFile image : images) {
                // Upload de l'image sur Cloudinary
                String imageUrl = cloudinaryService.uploadImage(image, "medecin_images");

                // Création de l'objet MedecinImage et sauvegarde
                MedecinImage medecinImage = new MedecinImage();
                medecinImage.setImageUrl(imageUrl);
                medecinImage.setDescription(image.getOriginalFilename() + " - " + description + " - " + type + " - " + image.getSize());
                medecinImage.setType(type);
                medecinImage.setMedecin(medecin);
                medecinImage.setActive(true);
                medecinImageRepository.save(medecinImage);
            }

            return ResponseEntity.ok("Images uploadées avec succès.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erreur lors de l'upload des images.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Endpoint pour supprimer une image associée à un médecin
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        try {
            // Récupérer l'image par son ID
            MedecinImage medecinImage = medecinImageService.getImageById(id)
                    .orElseThrow(() -> new NotFoundException("Image non trouvée"));

            String imageUrl = medecinImage.getImageUrl();
            cloudinaryService.deleteImage(imageUrl);

            // Supprimer l'image de la base de données
            medecinImageRepository.deleteById(id);

            return ResponseEntity.ok("Image supprimée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression de l'image.");
        }
    }
    /**
     * Mettre à jour les informations d'une image
     */
    @PutMapping("/image/{imageId}")
    public ResponseEntity<?> updateImage(
            @PathVariable Long imageId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isActive", required = false) Boolean isActive) {

        try {
            MedecinImage updatedImage = medecinImageService.updateImage(
                    imageId,
                    description,
                    isActive != null ? isActive : false);

            // Synchroniser les champs photo_med et photo_couverture_med du médecin
            //medecinImageService.synchronizeMedecinPhotos(updatedImage.getMedecin().getId());

            return ResponseEntity.ok(updatedImage);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Définir une image comme active
     */
    @PutMapping("/{medecinId}/set-active/{imageId}")
    public ResponseEntity<?> setActiveImage(
            @PathVariable Long medecinId,
            @PathVariable Long imageId) {

        try {
            MedecinImage activeImage = medecinImageService.setActiveImage(medecinId, imageId);

            // Synchroniser les champs photo_med et photo_couverture_med du médecin
            //medecinImageService.synchronizeMedecinPhotos(medecinId);

            return ResponseEntity.ok(activeImage);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Supprimer une image
     */
    @DeleteMapping("/{medecinId}/image/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long medecinId,
            @PathVariable Long imageId) {

        try {
            medecinImageService.deleteImage(medecinId, imageId);

            // Synchroniser les champs photo_med et photo_couverture_med du médecin
            //medecinImageService.synchronizeMedecinPhotos(medecinId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Image supprimée avec succès");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la suppression du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Supprimer toutes les images d'un médecin
     */
    @DeleteMapping("/{medecinId}")
    public ResponseEntity<?> deleteAllImages(@PathVariable Long medecinId) {
        try {
            medecinImageService.deleteAllImagesByMedecinId(medecinId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Toutes les images ont été supprimées avec succès");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la suppression des fichiers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}