package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Antecedents;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.AntecedentRequest;
import com.clinitalPlatform.services.AntecedentsService;
import com.clinitalPlatform.util.GlobalVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antecedents")
@CrossOrigin(origins = "*" , maxAge = 3600) // À adapter selon ton domaine
public class AntecedentsController {

    @Autowired
    private AntecedentsService antecedentsService;
    @Autowired
    private GlobalVariables globalVariables;

    /**
     * Créer un antécédent lié à un dossier médical.
     */
    @PostMapping("/add/dossier/{dossierId}")
    public ResponseEntity<Antecedents> createAntecedent(
            @PathVariable Long dossierId,
            @RequestBody AntecedentRequest request
    ) {
        Antecedents antecedent = antecedentsService.createAntecedent(request, dossierId);
        return ResponseEntity.ok(antecedent);
    }

    @PostMapping("/batch/dossier/{dossierId}")
    public ResponseEntity<List<Antecedents>> createMultipleAntecedents(
            @PathVariable Long dossierId,
            @RequestBody List<AntecedentRequest> requestList
    ) {
        List<Antecedents> created = antecedentsService.createMultipleAntecedents(requestList , dossierId);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/batch-upsert/dossier/{dossierId}")
    public ResponseEntity<List<Antecedents>> createOrUpdateMultipleAntecedents(
            @PathVariable Long dossierId,
            @RequestBody List<AntecedentRequest> requestList
    ) {
        try {
            User user = globalVariables.getConnectedUser();
            List<Antecedents> result = antecedentsService.createOrUpdateMultipleAntecedents(
                    requestList , dossierId , user
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Gérer l'erreur de manière appropriée
        }
    }

    /**
     * Mettre à jour un antécédent par ID.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Antecedents> updateAntecedent(
            @PathVariable Long id,
            @RequestBody AntecedentRequest request
    ) {
        Antecedents updated = antecedentsService.updateAntecedent(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprimer un antécédent.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAntecedent(@PathVariable Long id) {
        antecedentsService.deleteAntecedent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer un antécédent par ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Antecedents> getAntecedentById(@PathVariable Long id) {
        Antecedents antecedent = antecedentsService.getAntecedentById(id);
        return ResponseEntity.ok(antecedent);
    }

    /**
     * Récupérer tous les antécédents.
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<Antecedents>> getAllAntecedents() {
        return ResponseEntity.ok(antecedentsService.getAllAntecedents());
    }

    /**
     * Récupérer les antécédents par dossier médical.
     */
    @GetMapping("/dossier/{dossierId}")
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEDECIN' , 'ROLE_PATIENT')")
    public ResponseEntity<List<Antecedents>> getByDossierId(@PathVariable Long dossierId) {
        return ResponseEntity.ok(antecedentsService.getAntecedentsByDossierId(dossierId));
    }
}
