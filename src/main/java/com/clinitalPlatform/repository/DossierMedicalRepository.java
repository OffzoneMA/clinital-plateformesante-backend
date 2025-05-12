package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.DossierMedical;


@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {
}
