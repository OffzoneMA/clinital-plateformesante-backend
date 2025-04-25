package com.clinitalPlatform.services;

import com.clinitalPlatform.models.ExpertisesMedecin;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.ExpertisesMedecinRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpertisesMedecinService {

    @Autowired
    private ExpertisesMedecinRepository expertisesRepo;

    @Autowired
    private MedecinRepository medecinRepo;

    public List<ExpertisesMedecin> getAllExpertises() {
        return expertisesRepo.findAll();
    }

    public ExpertisesMedecin addExpertise(String nomExp) {
        if (expertisesRepo.findByNom_exp(nomExp).isPresent()) {
            throw new IllegalArgumentException("Cette expertise existe déjà.");
        }
        ExpertisesMedecin exp = new ExpertisesMedecin();
        exp.setNom_exp(nomExp);
        return expertisesRepo.save(exp);
    }

    public ExpertisesMedecin updateExpertise(Long id, String nouveauNom) throws NotFoundException {
        ExpertisesMedecin expertise = expertisesRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Expertise non trouvée"));
        expertise.setNom_exp(nouveauNom);
        return expertisesRepo.save(expertise);
    }

    public void deleteExpertise(Long id) throws NotFoundException {
        ExpertisesMedecin expertise = expertisesRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Expertise non trouvée"));
        expertisesRepo.delete(expertise);
    }


    public Medecin assignExpertisesToMedecin(Long medecinId, List<Long> expertisesIds) throws NotFoundException {
        Medecin medecin = medecinRepo.findById(medecinId)
                .orElseThrow(() -> new NotFoundException("Médecin non trouvé"));

        List<ExpertisesMedecin> expertises = expertisesRepo.findAllById(expertisesIds);
        medecin.setExpertises_med(expertises);

        return medecinRepo.save(medecin);
    }

    public List<ExpertisesMedecin> getExpertisesForMedecin(Long medecinId) throws NotFoundException {
        Medecin medecin = medecinRepo.findById(medecinId)
                .orElseThrow(() -> new NotFoundException("Médecin non trouvé"));

        return medecin.getExpertises_med();
    }
}

