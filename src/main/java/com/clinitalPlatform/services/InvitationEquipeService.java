package com.clinitalPlatform.services;

import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.enums.InvitationStatus;
import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.InvitationEquipe;
import com.clinitalPlatform.repository.InvitationEquipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class InvitationEquipeService {

    @Autowired
    private InvitationEquipeRepository invitationRepository;

    @Autowired
    private EmailSenderService emailService;

    /**
     * Get all invitations
     * */
    public List<InvitationEquipe> getAllInvitations() {
        return invitationRepository.findAll();
    }

    /**
     * Envoie une invitation à un membre de l'équipe
     */
    public InvitationEquipe envoyerInvitation(String email, ERole role, Cabinet cabinet) {
        // Vérifier si une invitation déjà envoyée est encore valide
        InvitationEquipe existing = invitationRepository
                .findByEmailAndCabinetIdAndStatus(email, cabinet.getId_cabinet(), InvitationStatus.EN_COURS);
        if (existing != null) {
            throw new IllegalStateException("Une invitation en cours existe déjà pour cet email.");
        }

        InvitationEquipe invitation = new InvitationEquipe();
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setCabinet(cabinet);
        invitation.setStatus(InvitationStatus.EN_COURS);
        invitation.setDateEnvoi(LocalDateTime.now());
        invitation.setDateExpiration(LocalDateTime.now().plusDays(30)); // expire après 30 jours
        invitation.setToken(UUID.randomUUID().toString()); // token unique

        invitationRepository.save(invitation);

        // Envoi de l'e-mail avec le lien unique
        emailService.sendInvitationEquipe(email, role, cabinet, cabinet.getCreator());

        return invitation;
    }

    /**
     * Accepte une invitation
     */
    public void accepterInvitation(String token) {
        InvitationEquipe invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitation non trouvée"));

        if (invitation.getStatus() != InvitationStatus.EN_COURS) {
            throw new IllegalStateException("L'invitation n'est pas valide ou déjà traitée.");
        }
        if (isInvitationExpired(invitation)) {
            throw new IllegalStateException("L'invitation a expiré.");
        }

        // TODO : Logique pour rattacher l'utilisateur au cabinet avec le bon rôle

        invitation.setStatus(InvitationStatus.TRAITER);
        invitation.setDateAccepted(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    /**
     * Relance une invitation avant expiration
     */
    public InvitationEquipe relancerInvitation(Long id) {
        InvitationEquipe invitation = getInvitationById(id);
        if (invitation.getStatus() != InvitationStatus.EN_COURS) {
            throw new IllegalStateException("Impossible de relancer une invitation traitée ou expirée.");
        }

        invitation.setDateEnvoi(LocalDateTime.now());
        invitation.setDateExpiration(LocalDateTime.now().plusDays(30));

        emailService.sendInvitationEquipe(invitation.getEmail(), invitation.getRole(), invitation.getCabinet(), invitation.getCabinet().getCreator());

        return invitationRepository.save(invitation);
    }

    public InvitationEquipe updateInvitationStatus(Long id, InvitationStatus status) {
        InvitationEquipe invitation = getInvitationById(id);
        invitation.setStatus(status);
        if (status == InvitationStatus.EXPIRER) {
            invitation.setDateExpiration(LocalDateTime.now());
        }
        return invitationRepository.save(invitation);
    }

    public void supprimerInvitation(Long id) {
        log.info("Suppression de l'invitation avec l'ID : {}", id);
        InvitationEquipe invitation = invitationRepository.getById(id);
        invitationRepository.deleteById(invitation.getId());
        log.info("Invitation supprimée avec succès.");
    }

    public boolean isInvitationExpired(InvitationEquipe invitation) {
        return invitation.getDateExpiration().isBefore(LocalDateTime.now());
    }

    public void cleanExpiredInvitations() {
        LocalDateTime now = LocalDateTime.now();
        invitationRepository.findAll().stream()
                .filter(invitation -> invitation.getStatus() == InvitationStatus.EN_COURS &&
                        invitation.getDateExpiration().isBefore(now))
                .forEach(invitation -> {
                    invitation.setStatus(InvitationStatus.EXPIRER);
                    invitationRepository.save(invitation);
                });
    }

    public InvitationEquipe getInvitationById(Long id) {
        return invitationRepository.getById(id);
    }

    public List<InvitationEquipe> getInvitationByCabinetId(Long cabinetId) {
        return invitationRepository.findByCabinetId(cabinetId);
    }
}
