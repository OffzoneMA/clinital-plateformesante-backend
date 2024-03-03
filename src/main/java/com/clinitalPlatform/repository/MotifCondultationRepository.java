package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.MotifConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface MotifCondultationRepository extends JpaRepository<MotifConsultation,Long> {
    

    @Query(value ="SELECT * FROM motifs_consultation WHERE id_motif = :id",nativeQuery = true)
	MotifConsultation getById(Long id); 
}
