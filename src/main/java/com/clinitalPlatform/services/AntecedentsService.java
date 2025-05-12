package com.clinitalPlatform.services;

import com.clinitalPlatform.enums.AntecedentTypeEnum;
import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.Antecedents;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.payload.request.AntecedentRequest;
import com.clinitalPlatform.repository.AntecedentsRepository;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AntecedentsService {

    @Autowired
    private AntecedentsRepository antecedentsRepository;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    public Antecedents createAntecedent(AntecedentRequest request, Long dossierId) {
        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("DossierMedical", "id", dossierId));

        Antecedents antecedent = new Antecedents();
        antecedent.setDossier(dossier);
        antecedent.setDescription(request.getDescription());
        antecedent.setDate(LocalDate.now());

        try {
            AntecedentTypeEnum typeEnum = AntecedentTypeEnum.valueOf(request.getType().toUpperCase());
            antecedent.setType(typeEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type d'antécédent invalide : " + request.getType());
        }

        return antecedentsRepository.save(antecedent);
    }

    public List<Antecedents> createMultipleAntecedents(List<AntecedentRequest> requests, Long dossierId) {
        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("DossierMedical", "id", dossierId));

        List<Antecedents> result = new ArrayList<>();

        for (AntecedentRequest request : requests) {
            Antecedents antecedent = new Antecedents();
            antecedent.setDescription(request.getDescription());
            antecedent.setDate(LocalDate.now());
            antecedent.setDossier(dossier);

            try {
                AntecedentTypeEnum typeEnum = AntecedentTypeEnum.valueOf(request.getType().toUpperCase());
                antecedent.setType(typeEnum);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Type d'antécédent invalide : " + request.getType());
            }

            result.add(antecedent);
        }

        return antecedentsRepository.saveAll(result);
    }

    public List<Antecedents> createOrUpdateMultipleAntecedents(List<AntecedentRequest> requests, Long dossierId) {
        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("DossierMedical", "id", dossierId));

        List<Antecedents> result = new ArrayList<>();

        for (AntecedentRequest request : requests) {
            AntecedentTypeEnum typeEnum;
            try {
                typeEnum = AntecedentTypeEnum.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Type d'antécédent invalide : " + request.getType());
            }

            // Recherche d’un antécédent existant par type et dossier
            Optional<Antecedents> existingOptional = antecedentsRepository
                    .findByDossierIdAndType(dossierId, typeEnum);

            Antecedents antecedent;
            if (existingOptional.isPresent()) {
                antecedent = existingOptional.get();
                antecedent.setDescription(request.getDescription());
                antecedent.setDate(LocalDate.now());
            } else {
                antecedent = new Antecedents();
                antecedent.setType(typeEnum);
                antecedent.setDescription(request.getDescription());
                antecedent.setDate(LocalDate.now());
                antecedent.setDossier(dossier);
            }

            result.add(antecedent);
        }

        return antecedentsRepository.saveAll(result);
    }


    public Antecedents updateAntecedent(Long id, AntecedentRequest request) {
        Antecedents existing = antecedentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Antecedents", "id", id));

        existing.setDescription(request.getDescription());

        try {
            AntecedentTypeEnum typeEnum = AntecedentTypeEnum.valueOf(request.getType().toUpperCase());
            existing.setType(typeEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type d'antécédent invalide : " + request.getType());
        }

        return antecedentsRepository.save(existing);
    }

    public void deleteAntecedent(Long id) {
        if (!antecedentsRepository.existsById(id)) {
            throw new ResourceNotFoundException( "Antécédent" , "id" , id);
        }
        antecedentsRepository.deleteById(id);
    }

    public Antecedents getAntecedentById(Long id) {
        return antecedentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Antecedents", "id", id));
    }


    public List<Antecedents> getAllAntecedents() {
        return antecedentsRepository.findAll();
    }

    public List<Antecedents> getAntecedentsByDossierId(Long dossierId) {
        return antecedentsRepository.findByDossierId(dossierId);
    }
}
