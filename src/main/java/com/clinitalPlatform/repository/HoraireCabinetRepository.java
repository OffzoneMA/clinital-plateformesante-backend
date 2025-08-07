package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.HoraireCabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HoraireCabinetRepository extends JpaRepository<HoraireCabinet, Long> {
    @Query(value = "SELECT * FROM horaire_cabinet WHERE cabinet_id = :cabinetId", nativeQuery = true)
    List<HoraireCabinet> findByCabinetId(@Param("cabinetId") Long cabinetId);
}
