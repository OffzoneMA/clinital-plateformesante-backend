package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.DiplomeMedecin;
import com.clinitalPlatform.models.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiplomeMedecinRepository extends JpaRepository<DiplomeMedecin,Long> {
    List<DiplomeMedecin> findByMedecin(Medecin medecin);
}
