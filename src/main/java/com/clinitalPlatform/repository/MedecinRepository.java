package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Medecin;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
	
}
