package com.clinitalPlatform.controllers;

import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.enums.InvitationStatus;
import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.InvitationEquipe;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.InvitationEquipeRequest;
import com.clinitalPlatform.services.CabinetServiceImpl;
import com.clinitalPlatform.services.InvitationEquipeService;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.util.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invitations-equipe")
public class InvitationEquipeController {

    @Autowired
    private InvitationEquipeService invitationService;

    @Autowired
    private CabinetServiceImpl cabinetService;
    @Autowired
    private GlobalVariables globalVariables;
    @Autowired
    private MedecinServiceImpl medecinServiceImpl;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEDECIN')")
    public ResponseEntity<?> getAllInvitations() {
        try {
            List<InvitationEquipe> invitations = invitationService.getAllInvitations();
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la récupération des invitations : " + e.getMessage());
        }
    }

    @PostMapping("/envoyer/{cabinetId}")
    public ResponseEntity<?> envoyerInvitation(@RequestBody InvitationEquipeRequest invitationRequest, @PathVariable Long cabinetId)  {
        try {

            Optional<Cabinet> cabinet = cabinetService.findById(cabinetId);
            if (cabinet.isEmpty()) {
                return ResponseEntity.badRequest().body("Cabinet introuvable avec l'ID : " + cabinetId);
            }

            Cabinet c = cabinet.get();
            return ResponseEntity.ok(invitationService.envoyerInvitation(invitationRequest.getEmail(), invitationRequest.getRole(), c));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de l'envoi de l'invitation : " + e.getMessage());
        }
    }

    @PostMapping("/envoyer/connected-medecin")
    @PreAuthorize("hasAnyRole('ROLE_MEDECIN')")
    public ResponseEntity<?> envoyerInvitationByConnectedMedecin(@RequestBody InvitationEquipeRequest invitationRequest) {
        try {
            log.info("Envoyer l'invitation : " + invitationRequest);
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userId);
            if (medecin == null) {
                throw new RuntimeException("Médecin introuvable pour l'utilisateur connecté (ID utilisateur : " + userId + ")");
            }
            Long cabinetId = medecin.getFirstCabinetId();

            Optional<Cabinet> cabinet = cabinetService.findById(cabinetId);
            if (cabinet.isEmpty()) {
                return ResponseEntity.badRequest().body("Cabinet introuvable avec l'ID : " + cabinetId);
            }

            Cabinet c = cabinet.get();
            log.info("Envoyer l'invitation en cours. Cabinet : " + c.getId_cabinet() + ", Email : " + invitationRequest.getEmail() + ", Role : " + invitationRequest.getRole());
            return ResponseEntity.ok(invitationService.envoyerInvitation(invitationRequest.getEmail(), invitationRequest.getRole() , c));

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'invitation : " + e.getMessage(), e);
            return ResponseEntity.status(404).body("Erreur lors de l'envoi de l'invitation : " + e.getMessage());
        }
    }

    @PostMapping("/accepter/{token}")
    public ResponseEntity<?> accepterInvitation(@PathVariable String token) {
        try {
            invitationService.accepterInvitation(token);
            return ResponseEntity.ok("Invitation acceptée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de l'acceptation de l'invitation : " + e.getMessage());
        }
    }

    @PostMapping("/relancer/{id}")
    public ResponseEntity<?> relancerInvitation(@PathVariable Long id) {
        try {
            InvitationEquipe invitation = invitationService.relancerInvitation(id);
            if (invitation == null) {
                return ResponseEntity.status(404).body("Invitation introuvable avec l'ID : " + id);
            }
            return ResponseEntity.ok("Invitation relancée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la relance de l'invitation : " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam InvitationStatus status) {
        try {
            return ResponseEntity.ok(invitationService.updateInvitationStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la mise à jour du statut de l'invitation : " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerInvitation(@PathVariable Long id) {
        try {
            invitationService.supprimerInvitation(id);
            return ResponseEntity.ok("Invitation supprimée avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'invitation : " + e.getMessage(), e);
            return ResponseEntity.status(500).body("Erreur lors de la suppression de l'invitation : " + e.getMessage());
        }
    }

    @GetMapping("/cabinet/{cabinetId}")
    public ResponseEntity<?> getByCabinet(@PathVariable Long cabinetId) {
        try {
            Optional<Cabinet> cabinet = cabinetService.findById(cabinetId);
            if (cabinet.isEmpty()) {
                return ResponseEntity.status(404).body("Cabinet introuvable avec l'ID : " + cabinetId);
            }
            List<InvitationEquipe> invitations = invitationService.getInvitationByCabinetId(cabinetId);
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la récupération des invitations : " + e.getMessage());
        }
    }

    @GetMapping("/cabinet/connected-medecin")
    @PreAuthorize("hasAnyRole('ROLE_MEDECIN')")
    public ResponseEntity<?> getByCabinetConnectedMedecin() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin introuvable pour l'utilisateur connecté (ID utilisateur : " + userId + ")");
            }
            Long cabinetId = medecin.getFirstCabinetId();
            Optional<Cabinet> cabinet = cabinetService.findById(cabinetId);
            if (cabinet.isEmpty()) {
                return ResponseEntity.status(404).body("Cabinet introuvable avec l'ID : " + cabinetId);
            }
            List<InvitationEquipe> invitations = invitationService.getInvitationByCabinetId(cabinetId);
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la récupération des invitations : " + e.getMessage());
        }
    }
}

