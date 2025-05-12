package com.clinitalPlatform.services;

import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Observation;
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

    public Observation ajouterObservation(ObservationRequest request ,  Long dossierId) {
        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("DossierMedical", "id", dossierId));

        Observation obs = new Observation();
        obs.setContenu(request.getContenu());
        obs.setDateObservation(LocalDateTime.now());
        obs.setDossier(dossier);

        return observationRepository.save(obs);
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

