package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.ExpertisesMedecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpertisesMedecinRepository extends JpaRepository<ExpertisesMedecin, Long> {

    @Query(value = "SELECT * FROM expertises WHERE nom_exp = ?1", nativeQuery = true)
    Optional<ExpertisesMedecin> findByNom_exp(String nom_exp);
}

