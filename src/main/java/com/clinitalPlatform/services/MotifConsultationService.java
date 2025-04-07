package com.clinitalPlatform.services;

import com.clinitalPlatform.models.MotifConsultation;
import com.clinitalPlatform.enums.MotifConsultationEnum;

import com.clinitalPlatform.repository.MotifConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MotifConsultationService {

    @Autowired
    private MotifConsultationRepository motifConsultationRepository;

    public List<MotifConsultation> getAllMotifConsultations() {
        return motifConsultationRepository.findAll();
    }

    public Optional<MotifConsultation> getMotifConsultationById(Long id) {
        return motifConsultationRepository.findById(id);
    }

    public MotifConsultation getMotifConsultationByMotif(MotifConsultationEnum motif) {
        return motifConsultationRepository.findByMotif(motif);
    }

    public MotifConsultation saveMotifConsultation(MotifConsultation motifConsultation) {
        return motifConsultationRepository.save(motifConsultation);
    }

    public void deleteMotifConsultation(Long id) {
        motifConsultationRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return motifConsultationRepository.existsById(id);
    }
}