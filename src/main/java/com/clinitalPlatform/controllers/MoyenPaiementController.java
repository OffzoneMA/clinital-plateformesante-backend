package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.MoyenPaiementDTO;
import com.clinitalPlatform.enums.TypeMoyenPaiementEnum;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MoyenPaiement;
import com.clinitalPlatform.services.MoyenPaiementService;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.util.GlobalVariables;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/moyens-paiement/")
public class MoyenPaiementController {

    @Autowired
    private MoyenPaiementService moyenPaiementService;
    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private MedecinService medecinService;


    //Récupérer tous les moyens de paiement
    @GetMapping("/all-methodes")
    public ResponseEntity<?> getAllPaymentMethods() {
        List<MoyenPaiementDTO> paymentMethods = moyenPaiementService.getAllMoyensPaiement();
        return ResponseEntity.ok(paymentMethods);
    }

    // Ajouter un moyen de paiement
    @PostMapping("/add")
    public ResponseEntity<MoyenPaiementDTO> addPaymentMethod(@RequestBody MoyenPaiementDTO moyenPaiementDTO) {
        MoyenPaiementDTO created = moyenPaiementService.createMoyenPaiement(TypeMoyenPaiementEnum.getEnumByString(moyenPaiementDTO.getType()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Récupérer un moyen de paiement par ID
    @GetMapping("/{id}")
    public ResponseEntity<MoyenPaiementDTO> getPaymentMethodById(@PathVariable Long id) {
        return moyenPaiementService.getMoyenPaiementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Supprimer un moyen de paiement
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        try {
            moyenPaiementService.deleteMoyenPaiement(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Associer des moyens de paiement à un médecin
    @PostMapping("/associer-med/{id}")
    public ResponseEntity<Medecin> assignPaymentMethods(
            @PathVariable Long id,
            @RequestBody List<Long> paymentMethodIds
    ) {
        try {
            Medecin updated = moyenPaiementService.addPaymentMethodsToMedecin(id, paymentMethodIds);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/associer-med/connected")
    public ResponseEntity<?> assignPaymentMethodsToConnectedMedecin(
            @RequestBody List<Long> paymentMethodIds
    ) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin connectedMedecin = medecinService.getMedecinByUserId(userId);
            Medecin updated = moyenPaiementService.addPaymentMethodsToMedecin(connectedMedecin.getId(), paymentMethodIds);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException | NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // Obtenir les moyens de paiement d’un médecin
    @GetMapping("/by-med/{id}")
    public ResponseEntity<List<MoyenPaiement>> getPaymentMethodsForMedecin(@PathVariable Long id) {
        try {
            List<MoyenPaiement> methods = moyenPaiementService.getMedecinPaymentMethods(id);
            return ResponseEntity.ok(methods);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

