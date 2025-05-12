package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ObservationRepository extends JpaRepository<Observation, Long> {

    @Query("SELECT o FROM Observation o WHERE o.dossier.id_dossier = :dossierId")
    List<Observation> findByDossierId(Long dossierId);
}

