package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Medecin;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    @Query(value = "SELECT m.* FROM medecins m WHERE m.user_id= :id",nativeQuery = true)
    public Medecin getMedecinByUserId(@Param("id")long id);
	
}
