package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Allergies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergiesRepository extends JpaRepository<Allergies,Long> {
    
}
