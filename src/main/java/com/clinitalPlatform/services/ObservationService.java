package com.clinitalPlatform.services;

import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Observation;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.ObservationRequest;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import com.clinitalPlatform.repository.ObservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ObservationService {

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;
    @Autowired
    private MedecinServiceImpl medecinServiceImpl;

    public Observation ajouterObservation(ObservationRequest request, Long dossierId, User user) throws Exception {
        // Vérifier l'existence du dossier
        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("DossierMedical", "id", dossierId));

        // Récupérer la dernière observation du dossier
        List<Observation> observations = observationRepository.findByDossierId(dossierId);
        Observation lastObservation = observations.isEmpty() ? null : observations.get(observations.size() - 1);

        // Récupérer le nom de l'utilisateur si c'est un médecin
        String updatedBy = null;
        if (user.getRole() != null && user.getRole().name().equals("ROLE_MEDECIN")) {
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(user.getId());
            updatedBy = medecin.getNom_med() + " " + medecin.getPrenom_med();
        }

        // Cas 1 : Ajouter au contenu de la dernière observation si elle existe
        if (lastObservation != null) {
            String newContenu = request.getContenu();
            lastObservation.setContenu(newContenu);
            if (updatedBy != null) {
                lastObservation.setUpdatedBy(updatedBy);
            }
            return observationRepository.save(lastObservation);
        }

        // Cas 2 : Créer une nouvelle observation
        Observation newObservation = new Observation();
        newObservation.setContenu(request.getContenu());
        newObservation.setDateObservation(LocalDateTime.now());
        newObservation.setDossier(dossier);
        if (updatedBy != null) {
            newObservation.setUpdatedBy(updatedBy);
        }

        return observationRepository.save(newObservation);
    }

    public List<Observation> getObservationsByDossier(Long dossierId) {
        return observationRepository.findByDossierId(dossierId);
    }

    public void deleteObservation(Long observationId) {
        Observation obs = observationRepository.findById(observationId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation", "id", observationId));
        observationRepository.delete(obs);
    }

    public Observation updateObservation(Long observationId, String contenu) {
        Observation obs = observationRepository.findById(observationId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation", "id", observationId));
        obs.setContenu(contenu);
        return observationRepository.save(obs);
    }

    public List<Observation> getAllObservations() {
        return observationRepository.findAll();
    }
}

