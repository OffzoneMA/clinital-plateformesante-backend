package com.clinitalPlatform.services;

import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DossierMedicalService {

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    public List<DossierMedical> getAll() {
        return dossierMedicalRepository.findAll();
    }

    public Optional<DossierMedical> getById(Long id) {
        return dossierMedicalRepository.findById(id);
    }

    public DossierMedical save(DossierMedical dossier) {
        return dossierMedicalRepository.save(dossier);
    }

    public void delete(Long id) {
        dossierMedicalRepository.deleteById(id);
    }
    
}

