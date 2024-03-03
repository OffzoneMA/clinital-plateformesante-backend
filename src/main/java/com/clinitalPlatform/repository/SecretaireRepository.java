package com.clinitalPlatform.repository;


import com.clinital.models.Secretaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SecretaireRepository extends JpaRepository<Secretaire, Long>{
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM secretaire_cabinet WHERE secrt_id=?1 AND cabinet_id=?2",nativeQuery = true)
    public static boolean deleteByIdfromCabinet(long idsec,long idcabinet){
        return true;
    };

    @Query(value ="SELECT * FROM secretaires WHERE id = :id",nativeQuery = true)
	Secretaire getById(Long id);

}
