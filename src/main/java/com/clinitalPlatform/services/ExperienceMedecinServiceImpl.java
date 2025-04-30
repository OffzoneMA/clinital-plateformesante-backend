package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.ExperienceMedecinDTO;
import com.clinitalPlatform.models.ExperienceMedecin;
import com.clinitalPlatform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperienceMedecinServiceImpl implements ExperienceMedecinService {

    private static final Logger log = LoggerFactory.getLogger(ExperienceMedecinServiceImpl.class);
    @Autowired
    private ExperienceMedecinRepository repository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private VilleRepository villeRepository;

    @Override
    public ExperienceMedecinDTO createExperience(ExperienceMedecinDTO dto) {
        ExperienceMedecin experience = dtoToEntity(dto);
        return entityToDto(repository.save(experience));
    }

    @Override
    public List<ExperienceMedecinDTO> createMultipleExperiences(List<ExperienceMedecinDTO> dtos) {

        return dtos.stream()
                .map(this::dtoToEntity)
                .map(repository::save)
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }


    @Override
    public ExperienceMedecinDTO updateExperience(Long id, ExperienceMedecinDTO dto) {
        ExperienceMedecin existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expérience non trouvée"));
        existing.setNom_experience(dto.getNom_experience());
        existing.setEtablissement(dto.getEtablissement());
        existing.setAnnee_debut(dto.getAnnee_debut());
        existing.setAnnee_fin(dto.getAnnee_fin());
        existing.setPost_actuel(dto.isPost_actuel());
        existing.setEmplacement(villeRepository.findById(dto.getEmplacementId()).orElse(null));
        return entityToDto(repository.save(existing));
    }

    @Override
    public void deleteExperience(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<ExperienceMedecin> getAllExperiencesByMedecin(Long medecinId) {
        return repository.findByMedecinId(medecinId);
    }

    public ExperienceMedecin dtoToEntity(ExperienceMedecinDTO dto) {
        ExperienceMedecin entity = new ExperienceMedecin();
        entity.setNom_experience(dto.getNom_experience());
        entity.setEtablissement(dto.getEtablissement());
        entity.setAnnee_debut(dto.getAnnee_debut());
        entity.setAnnee_fin(dto.getAnnee_fin());
        entity.setPost_actuel(dto.isPost_actuel());
        entity.setEmplacement(villeRepository.findById(dto.getEmplacementId()).orElse(null));
        if(dto.getMedecinId() != null && dto.getMedecinId() != 0) {
            entity.setMedecin(medecinRepository.findById(dto.getMedecinId()).orElse(null));
        }
        return entity;
    }

    private ExperienceMedecinDTO entityToDto(ExperienceMedecin entity) {
        ExperienceMedecinDTO dto = new ExperienceMedecinDTO();
        dto.setId(entity.getId());
        dto.setNom_experience(entity.getNom_experience());
        dto.setEtablissement(entity.getEtablissement());
        dto.setAnnee_debut(entity.getAnnee_debut());
        dto.setAnnee_fin(entity.getAnnee_fin());
        dto.setPost_actuel(entity.isPost_actuel());
        dto.setEmplacementId(entity.getEmplacement() != null ? entity.getEmplacement().getId_ville() : null);
        dto.setMedecinId(entity.getMedecin() != null ? entity.getMedecin().getId() : null);
        return dto;
    }
}

