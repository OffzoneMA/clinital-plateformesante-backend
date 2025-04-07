package com.clinitalPlatform.services;

import com.clinitalPlatform.models.ModeConsultation;
import com.clinitalPlatform.enums.ModeConsultationEnum;

import com.clinitalPlatform.repository.ModeConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModeConsultationService {

    @Autowired
    private ModeConsultationRepository modeConsultationRepository;

    public List<ModeConsultation> getAllModeConsultations() {
        return modeConsultationRepository.findAll();
    }

    public Optional<ModeConsultation> getModeConsultationById(Long id) {
        return modeConsultationRepository.findById(id);
    }

    public ModeConsultation getModeConsultationByMode(ModeConsultationEnum mode) {
        return modeConsultationRepository.findByMode(mode);
    }

    public ModeConsultation saveModeConsultation(ModeConsultation modeConsultation) {
        return modeConsultationRepository.save(modeConsultation);
    }

    public void deleteModeConsultation(Long id) {
        modeConsultationRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return modeConsultationRepository.existsById(id);
    }
}