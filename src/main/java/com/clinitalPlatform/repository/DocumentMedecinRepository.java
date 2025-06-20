package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.DocumentMedecin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentMedecinRepository extends JpaRepository<DocumentMedecin, Long> {
    List<DocumentMedecin> findByMedecinAuteurId(Long idMedecin);

    List<DocumentMedecin> findByMedecinsPartagesId(Long medecinId);

    List<DocumentMedecin> findByPatientsPartagesId(Long patientId);
}

