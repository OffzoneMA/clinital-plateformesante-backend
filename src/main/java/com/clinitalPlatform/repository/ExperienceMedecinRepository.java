package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.ExperienceMedecin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceMedecinRepository extends JpaRepository<ExperienceMedecin, Long> {
    List<ExperienceMedecin> findByMedecinId(Long medecinId);
}

