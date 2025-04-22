package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.MotifFermeture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MotifFermetureRepository extends JpaRepository<MotifFermeture, Long> {
    Optional<MotifFermeture> findByMotif(Enum motif);
}
