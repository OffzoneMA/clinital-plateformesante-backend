package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.FermetureDTO;
import com.clinitalPlatform.models.FermetureExceptionnelle;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.FermetureExceptionnelleService;
import com.clinitalPlatform.util.GlobalVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/fermetures")
public class FermetureExceptionnelleController {

    @Autowired
    private FermetureExceptionnelleService fermetureService;
    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private MedecinRepository medecinRepository;

    @GetMapping("/medecin/{id}")
    public List<FermetureExceptionnelle> getFermetures(@PathVariable Long id) {
        return fermetureService.getFermeturesParMedecin(id);
    }

    @GetMapping("/connected-medecin")
    public ResponseEntity<?> getFermeturesByConnectedMedecin() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinRepository.getMedecinByUserId(userId);

            if (medecin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Médecin introuvable pour l'utilisateur connecté (ID utilisateur : " + userId + ")");
            }

            List<FermetureExceptionnelle> fermetures = fermetureService.getFermeturesParMedecin(medecin.getId());
            return ResponseEntity.ok(fermetures);

        } catch (Exception e) {
            // Log technique (à adapter avec ton logger)
            System.err.println("Erreur lors de la récupération des fermetures : " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : impossible de récupérer les fermetures du médecin connecté.");
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody FermetureDTO fermetureDTO) {
        try {
            FermetureExceptionnelle fermeture = fermetureService.ajouterFermetureParIds(
                    fermetureDTO,
                    fermetureDTO.getMotifIds()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(fermeture);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de la création : " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody FermetureDTO fermetureDTO) {
        try {
            FermetureExceptionnelle updatedFermeture = fermetureService.updateFermeture(id, fermetureDTO);
            return ResponseEntity.ok(updatedFermeture);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            fermetureService.supprimerFermeture(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fermeture introuvable : " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<FermetureExceptionnelle>> getAllFermetures() {
        List<FermetureExceptionnelle> fermetures = fermetureService.getAllFermetures();
        return ResponseEntity.ok(fermetures);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            FermetureExceptionnelle fermeture = fermetureService.getFermetureById(id);
            return ResponseEntity.ok(fermeture);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fermeture introuvable");
        }
    }


}

