package com.clinitalPlatform.repository;

import com.clinital.models.DocumentsCabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentsCabinetRepository extends JpaRepository<DocumentsCabinet,Long>{
    
}
