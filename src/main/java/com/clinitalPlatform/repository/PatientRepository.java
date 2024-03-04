package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
	
}
