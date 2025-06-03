package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Tarif;
import com.clinitalPlatform.payload.request.TarifRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.services.TarifServiceImpl;
import com.clinitalPlatform.util.GlobalVariables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequestMapping("/api/tarifmed")
public class TarifController {
    @Autowired
    private TarifServiceImpl tarifService;
    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    MedecinServiceImpl medecinService;

    @PostMapping("/add")
    public ResponseEntity<?> addTarif(@RequestBody TarifRequest tarifRequest) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé");
            }

            Tarif newTarif = tarifService.save(tarifRequest , medecin);
            return ResponseEntity.ok(newTarif);
        } catch (Exception e) {
            log.error("Error while adding tarif", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add/connected")
    public ResponseEntity<?> addTarifForConnectedMedecin(@RequestBody TarifRequest tarifRequest) {
        try {
            log.info("Adding tarif for connected medecin" + tarifRequest);
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);

            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé");
            }

            Tarif newTarif = tarifService.save(tarifRequest , medecin);
            return ResponseEntity.ok(newTarif);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/add-multiple/connected")
    public ResponseEntity<?> addMultipleTarifsForConnectedMedecin(@RequestBody TarifRequest[] tarifRequests) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé");
            }
            for (TarifRequest tarifRequest : tarifRequests) {
                tarifService.save(tarifRequest , medecin);
            }
            return ResponseEntity.ok("Tarifs ajoutés avec succès");
        } catch (Exception e) {
            log.error("Error while adding multiple tarifs for connected medecin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{tarifId}")
    public ResponseEntity<?> updateTarif(@PathVariable Long tarifId, @RequestBody TarifRequest updatedTarifRequest) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé");
            }
            Tarif existingTarif = tarifService.findById(tarifId);
            if (existingTarif == null) {
                return ResponseEntity.status(404).body("Tarif non trouvé");
            }
            if (!existingTarif.getMedecin().getId().equals(medecin.getId())) {
                return ResponseEntity.status(403).body("Vous n'êtes pas autorisé à modifier ce tarif");
            }
            Tarif updated = tarifService.updateTarif(tarifId, updatedTarifRequest , medecin);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error while updating tarif with ID: {}", tarifId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{tarifId}")
    public ResponseEntity<?> getTarifById(@PathVariable Long tarifId) {
        Tarif tarif = tarifService.findById(tarifId);
        if (tarif != null) {
            return ResponseEntity.ok(tarif);
        } else {
            log.error("No tarif found with ID: {}", tarifId);
            return ResponseEntity.ok(new ApiResponse(false, "Aucun tarif trouvé"));
        }
    }

    @DeleteMapping("/delete/{tarifId}")
    public ResponseEntity<?> deleteTarif(@PathVariable Long tarifId) {
        try {
            tarifService.deleteTarif(tarifId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error while deleting tarif with ID: {}", tarifId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTarifs() {
        try {
            return ResponseEntity.ok(tarifService.getAllTarifs());
        } catch (Exception e) {
            log.error("Error while fetching all tarifs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/byMedecin/{medecinId}")
    public ResponseEntity<?> getTarifsByMedecinId(@PathVariable Long medecinId) {
        try {
            return ResponseEntity.ok(tarifService.getTarifsByMedecinId(medecinId));
        } catch (Exception e) {
            log.error("Error while fetching tarifs for medecin ID: {}", medecinId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/byConnectedMedecin")
    public ResponseEntity<?> getTarifsByConnectedMedecin() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé");
            }
            return ResponseEntity.ok(tarifService.getTarifsByMedecinId(medecin.getId()));
        } catch (Exception e) {
            log.error("Error while fetching tarifs for connected medecin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
