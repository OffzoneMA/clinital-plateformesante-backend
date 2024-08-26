package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Assistant;
import com.clinitalPlatform.models.Secretaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AssistantRepository extends JpaRepository<Assistant, Long>{



    @Query("SELECT s FROM Assistant s JOIN s.cabinet c WHERE c.id_cabinet = :cabinetId")
    List<Assistant> findAssistantsByCabinetId(@Param("cabinetId") Long cabinetId);


}
