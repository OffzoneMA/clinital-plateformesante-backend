package com.clinitalPlatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Cabinet;

@Repository
public interface CabinetRepository extends JpaRepository<Cabinet, Long>{
	
	    @Transactional
	    @Modifying
	    @Query(value="DELETE FROM cabinet WHERE id_cabinet = ?1",nativeQuery=true)
	    public void DeleteCabinetbyID(long Id);
	    
	    List<Cabinet> findByNomContainingIgnoreCase(String nom); 
	    
	    @Query(value ="SELECT * FROM cabinet WHERE id_cabinet = :id",nativeQuery = true)
		Cabinet getById(Long id);
	    
	    @Query(value="SELECT c.* FROM cabinet c, cabinet_medecins m WHERE c.id_cabinet=m.cabinet_id AND m.medecin_id=?1",nativeQuery=true)
	    public List<Cabinet> getAllCabinetByIdMed(long idmed);
}

