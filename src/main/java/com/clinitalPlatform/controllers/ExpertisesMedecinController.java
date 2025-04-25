package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.ExpertisesMedecin;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.services.ExpertisesMedecinService;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.util.GlobalVariables;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expertises")
@RequiredArgsConstructor
public class ExpertisesMedecinController {

    @Autowired
    private ExpertisesMedecinService expertisesService;

    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private MedecinService medecinService;

    // Récupérer toutes les expertises
    @GetMapping
    public ResponseEntity<List<ExpertisesMedecin>> getAllExpertises() {
        return ResponseEntity.ok(expertisesService.getAllExpertises());
    }

    //Ajouter une nouvelle expertise
    @PostMapping
    public ResponseEntity<?> addExpertise(@RequestBody String nomExp) {
        try {
            ExpertisesMedecin exp = expertisesService.addExpertise(nomExp);
            return ResponseEntity.ok(exp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Mettre à jour une expertise
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpertise(@PathVariable Long id, @RequestBody String nouveauNom) {
        try {
            ExpertisesMedecin updated = expertisesService.updateExpertise(id, nouveauNom);
            return ResponseEntity.ok(updated);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Supprimer une expertise
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpertise(@PathVariable Long id) {
        try {
            expertisesService.deleteExpertise(id);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Assigner une liste d'expertises à un médecin
    @PostMapping("/assign-to-medecin/{medecinId}")
    public ResponseEntity<?> assignExpertisesToMedecin(
            @PathVariable Long medecinId,
            @RequestBody List<Long> expertisesIds) {
        try {
            Medecin updated = expertisesService.assignExpertisesToMedecin(medecinId, expertisesIds);
            return ResponseEntity.ok(updated);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/assign-to-medecin/connected")
    public ResponseEntity<?> assignExpertisesToMedecin(
            @RequestBody List<Long> expertisesIds) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            Medecin updated = expertisesService.assignExpertisesToMedecin(medecin.getId(), expertisesIds);
            return ResponseEntity.ok(updated);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Récupérer les expertises d'un médecin
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<?> getExpertisesByMedecin(@PathVariable Long medecinId) {
        try {
            List<ExpertisesMedecin> expertises = expertisesService.getExpertisesForMedecin(medecinId);
            return ResponseEntity.ok(expertises);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/medecin/connected")
    public ResponseEntity<?> getExpertisesByConnectedMedecin() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            List<ExpertisesMedecin> expertises = expertisesService.getExpertisesForMedecin(medecin.getId());
            return ResponseEntity.ok(expertises);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

