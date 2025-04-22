package com.clinitalPlatform.services;

import com.clinitalPlatform.models.MotifFermeture;
import com.clinitalPlatform.repository.MotifFermetureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotifFermetureService {


    @Autowired
    private MotifFermetureRepository motifRepo;

    public List<MotifFermeture> findAll() {
        return motifRepo.findAll();
    }

    public MotifFermeture findByMotif(Enum motif) {
        return motifRepo.findByMotif(motif).orElse(null);
    }

    public MotifFermeture save(MotifFermeture motif) {
        return motifRepo.save(motif);
    }

    public MotifFermeture findById(Long id) {
        return motifRepo.findById(id).orElse(null);
    }

    public MotifFermeture update(Long id, MotifFermeture motif) {
        MotifFermeture existingMotif = motifRepo.findById(id).orElse(null);
        if (existingMotif != null) {
            existingMotif.setMotif(motif.getMotif());
            return motifRepo.save(existingMotif);
        }
        return null;
    }

    public void delete(MotifFermeture motif) {
        motifRepo.delete(motif);
    }
}
