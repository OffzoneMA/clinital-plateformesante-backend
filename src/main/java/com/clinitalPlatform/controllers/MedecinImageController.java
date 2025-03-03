package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.MedecinImage;
import com.clinitalPlatform.services.MedecinImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medecin-images")
@CrossOrigin(origins = "*")
public class MedecinImageController {

    @Autowired
    private MedecinImageService medecinImageService;

    /**
     * Récupérer toutes les images d'un médecin
     */
    @GetMapping("/{medecinId}")
    public ResponseEntity<List<MedecinImage>> getAllImages(@PathVariable Long medecinId) {
        return ResponseEntity.ok(medecinImageService.getAllImagesByMedecinId(medecinId));
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
    @PostMapping("/{medecinId}/upload")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long medecinId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description) {

        try {
            //MedecinImage savedImage = medecinImageService.uploadImage(medecinId, file, type, description);
            MedecinImage savedImage = new MedecinImage();

            // Synchroniser les champs photo_med et photo_couverture_med du médecin
            //medecinImageService.synchronizeMedecinPhotos(medecinId);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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