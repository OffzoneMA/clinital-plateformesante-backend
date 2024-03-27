package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Rendezvous;

@Repository
public interface RdvRepository extends JpaRepository<Rendezvous, Long> {
	
}
