package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.CompteRenduRdv;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.CompteRenduRdvRequest;
import com.clinitalPlatform.services.CompteRenduRdvService;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.util.GlobalVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compte-rendu")
public class CompteRenduRdvController {

    @Autowired
    private CompteRenduRdvService service;
    @Autowired
    private GlobalVariables globalVariables;
    @Autowired
    private MedecinServiceImpl medecinServiceImpl;

    // ✅ Créer un compte rendu
    @PostMapping("/create")
    public ResponseEntity<CompteRenduRdv> create( @RequestBody CompteRenduRdvRequest request) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userId);

            if (medecin == null) {
                return ResponseEntity.badRequest().body(null);
            }

            CompteRenduRdv cr = service.createCompteRendu(request, medecin);
            return ResponseEntity.ok(cr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ✅ Récupérer par ID
    @GetMapping("/{id}")
    public ResponseEntity<CompteRenduRdv> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Récupérer tous
    @GetMapping("/all")
    public ResponseEntity<List<CompteRenduRdv>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // ✅ Récupérer par patient
    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<List<CompteRenduRdv>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.findByPatientId(patientId));
    }

    // ✅ Récupérer par médecin
    @GetMapping("/by-medecin/{medecinId}")
    public ResponseEntity<List<CompteRenduRdv>> getByMedecin(@PathVariable Long medecinId) {
        return ResponseEntity.ok(service.findByMedecinId(medecinId));
    }

    // ✅ Récupérer par rendez-vous
    @GetMapping("/by-rdv/{rdvId}")
    public ResponseEntity<List<CompteRenduRdv>> getByRendezvous(@PathVariable Long rdvId) {
        return ResponseEntity.ok(service.findByRendezvousId(rdvId));
    }

    // ✅ Mettre à jour un compte rendu
    @PutMapping("/update/{id}")
    public ResponseEntity<CompteRenduRdv> update(@PathVariable Long id , @RequestBody CompteRenduRdvRequest compteRendu) {
        CompteRenduRdv updated = service.update(compteRendu , id);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // ✅ Supprimer un compte rendu
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
