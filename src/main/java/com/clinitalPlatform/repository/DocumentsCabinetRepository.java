package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.DocumentsCabinet;

import java.util.List;

@Repository
public interface DocumentsCabinetRepository extends JpaRepository<DocumentsCabinet,Long>{

    List<DocumentsCabinet> findByMedecinId(Long medecinId);

    @Query("SELECT d FROM DocumentsCabinet d WHERE d.cabinet.id_cabinet = :cabinetId")
    List<DocumentsCabinet> findByCabinetId(@Param("cabinetId") Long cabinetId);

}
