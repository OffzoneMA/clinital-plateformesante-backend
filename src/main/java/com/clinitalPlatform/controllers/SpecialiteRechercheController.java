package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.SpecialiteRechercheStats;
import com.clinitalPlatform.services.SpecialiteRechercheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/specialites_recherche/")
public class SpecialiteRechercheController {

    @Autowired
    private SpecialiteRechercheService rechercheService;

    @PostMapping("/increment/{specialiteId}")
    public ResponseEntity<String> incrementerRecherche(@PathVariable Long specialiteId) {
        rechercheService.incrementerRecherche(specialiteId);
        return ResponseEntity.ok("Recherche enregistrée.");
    }

    @GetMapping("/populaires")
    public ResponseEntity<List<SpecialiteRechercheStats>> getTopSpecialites(
            @RequestParam(defaultValue = "10") int limit) { // Par défaut, retourne 10 spécialités
        return ResponseEntity.ok(rechercheService.getTopSpecialites(limit));
    }
}
