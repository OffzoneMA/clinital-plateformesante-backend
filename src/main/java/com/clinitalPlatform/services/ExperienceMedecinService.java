package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.ExperienceMedecinDTO;
import com.clinitalPlatform.models.ExperienceMedecin;

import java.util.List;

public interface ExperienceMedecinService {
    ExperienceMedecinDTO createExperience(ExperienceMedecinDTO dto);
    ExperienceMedecinDTO updateExperience(Long id, ExperienceMedecinDTO dto);
    void deleteExperience(Long id);
    List<ExperienceMedecin> getAllExperiencesByMedecin(Long medecinId);
    List<ExperienceMedecinDTO> createMultipleExperiences(List<ExperienceMedecinDTO> dtos);

}
