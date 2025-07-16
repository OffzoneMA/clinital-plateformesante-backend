package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Observation;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.ObservationRequest;
import com.clinitalPlatform.services.ObservationService;
import com.clinitalPlatform.util.GlobalVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // À adapter selon ton domaine
@RestController
@RequestMapping("/api/observations")
public class ObservationController {

    @Autowired
    private ObservationService observationService;
    @Autowired
    private GlobalVariables globalVariables;

    @PostMapping("/add/dossier/{dossierId}")
    public ResponseEntity<Observation> addObservation(@RequestBody ObservationRequest request , @PathVariable Long dossierId) {
        try {
            User user = globalVariables.getConnectedUser();
            return ResponseEntity.ok(observationService.ajouterObservation(request , dossierId , user));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Gérer l'erreur de manière appropriée
        }
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<Observation>> getObservations(@PathVariable Long dossierId) {
        return ResponseEntity.ok(observationService.getObservationsByDossier(dossierId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Observation> updateObservation(@PathVariable Long id, @RequestBody ObservationRequest request) {
        return ResponseEntity.ok(observationService.updateObservation(id, request.getContenu()));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteObservation(@PathVariable Long id) {
        observationService.deleteObservation(id);
        return ResponseEntity.noContent().build();
    }
}

