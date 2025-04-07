package com.clinitalPlatform.repository;

import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.models.MotifConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotifConsultationRepository extends JpaRepository<MotifConsultation,Long> {
    

    @Query(value ="SELECT * FROM motifs_consultation WHERE id_motif = :id",nativeQuery = true)
    MotifConsultation getById(Long id);

    @Query(value = "SELECT id_motif FROM motifs_consultation WHERE libelle IN (:libelles)", nativeQuery = true)
    List<Long> findIdsByLibelles(@Param("libelles") List<String> libelles);

    MotifConsultation findByMotif(MotifConsultationEnum motif);
}
