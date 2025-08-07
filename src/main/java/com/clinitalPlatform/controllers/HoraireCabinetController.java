package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.HoraireCabinet;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.HoraireCabinetRequest;
import com.clinitalPlatform.services.HoraireCabinetService;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.util.GlobalVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horaires-cabinet")
public class HoraireCabinetController {

    @Autowired
    private HoraireCabinetService horaireCabinetService;
    @Autowired
    private GlobalVariables globalVariables;
    @Autowired
    private MedecinServiceImpl medecinServiceImpl;

    @PostMapping("/{cabinetId}/batch")
    public ResponseEntity<?> updateHorairesCabinet(
            @PathVariable Long cabinetId,
            @RequestBody List<HoraireCabinetRequest> horaires
    ) {
        try {
            horaireCabinetService.updateHorairesCabinet(cabinetId, horaires);
            return ResponseEntity.ok("Horaires enregistrés avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/batch/connected-med")
    public ResponseEntity<?> updateHorairesCabinetsByConnectedMed(@RequestBody List<HoraireCabinetRequest> horaires) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Médecin introuvable pour l'utilisateur connecté (ID utilisateur : " + userId + ")");
            }
            Long cabinetId = medecin.getFirstCabinetId();
            horaireCabinetService.updateHorairesCabinet(cabinetId, horaires);
            return ResponseEntity.ok("Horaires enregistrés avec succès pour le cabinet du médecin connecté.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : " + e.getMessage());
        }
    }

    // ✅ Récupération des horaires d'un cabinet
    @GetMapping("/{cabinetId}")
    public ResponseEntity<List<HoraireCabinet>> getHorairesCabinet(
            @PathVariable Long cabinetId
    ) {
        List<HoraireCabinet> horaires = horaireCabinetService.getHoraireCabinets(cabinetId);
        return ResponseEntity.ok(horaires);
    }

    @PreAuthorize("hasAnyRole('ROLE_MEDECIN')")
    @GetMapping("/connected-medecin")
    public ResponseEntity<?> getHorairesCabinetByConnectedMedecin() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Médecin introuvable pour l'utilisateur connecté (ID utilisateur : " + userId + ")");
            }
            Long cabinetId = medecin.getFirstCabinetId();
            List<HoraireCabinet> horaires = horaireCabinetService.getHoraireCabinets(cabinetId);
            return ResponseEntity.ok(horaires);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : impossible de récupérer les horaires du cabinet du médecin connecté.");
        }
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<?> getHorairesCabinetByConnectedMedecin(@PathVariable Long medecinId) {
        try {
            Medecin medecin = medecinServiceImpl.findById(medecinId);
            if (medecin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Médecin introuvable pour l'ID : " + medecinId);
            }
            Long cabinetId = medecin.getFirstCabinetId();
            List<HoraireCabinet> horaires = horaireCabinetService.getHoraireCabinets(cabinetId);
            return ResponseEntity.ok(horaires);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : impossible de récupérer les horaires du cabinet du médecin connecté.");
        }
    }

    @DeleteMapping("/delete/{horaireId}")
    public ResponseEntity<?> deleteHoraireCabinet(@PathVariable Long horaireId) {
        try {
            horaireCabinetService.deleteHoraireCabinet(horaireId);
            return ResponseEntity.ok("Horaire supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addHoraireCabinet(@RequestBody HoraireCabinetRequest request) {
        try {
            List<HoraireCabinet> horaire = horaireCabinetService.addHoraireCabinet(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(horaire);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Gérer l'erreur de manière appropriée
        }
    }

    @PutMapping("/update/{horaireId}")
    public ResponseEntity<?> updateHoraireCabinet(
            @PathVariable Long horaireId,
            @RequestBody HoraireCabinetRequest request
    ) {
        try {
            HoraireCabinet updatedHoraire = horaireCabinetService.updateHoraireCabinet(horaireId, request);
            return ResponseEntity.ok(updatedHoraire);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : " + e.getMessage());
        }
    }
}

