package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Secretaire;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SecretaireRepository extends JpaRepository<Secretaire, Long>{

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM secretaire_cabinet WHERE secrt_id=?1 AND cabinet_id=?2",nativeQuery = true)
    public static boolean deleteByIdfromCabinet(long idsec,long idcabinet){
        return true;
    };

    @Query("SELECT s FROM Secretaire s JOIN s.cabinet c WHERE c.id_cabinet = :cabinetId")
    List<Secretaire> findSecretairesByCabinetId(@Param("cabinetId") Long cabinetId);


}
