package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Tarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarifRepository extends JpaRepository<Tarif,Long> {
    List<Tarif> findByMedecinId(Long medecinId);
}
