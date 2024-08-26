package com.clinitalPlatform.repository;

import com.clinitalPlatform.dto.MedecinNetworkDTO;
import com.clinitalPlatform.models.MedecinNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedecinNetworkRepository extends JpaRepository<MedecinNetwork,Long> {

     @Query(value = "SELECT m.* FROM medecins m, medecin_network f WHERE m.id = f.id_medecin AND m.id = ?1", nativeQuery = true)
     List<MedecinNetworkDTO> getAllMedecinNetwork(long id_medecin) throws Exception;



    @Query(value = "SELECT f.* FROM medecin_network f WHERE  f.id_medecin = :id_medecin AND f.id_follower= :id_follower", nativeQuery = true)
    MedecinNetwork FindMedecinsNetworkByID(@Param("id_medecin")Long id_medecin, @Param("id_follower") Long id_follower) throws Exception;



    @Modifying
    @Query(value = "DELETE FROM medecin_network WHERE id_medecin = :id_medecin and id_follower = :id_follower", nativeQuery = true)
    void  deleteNetworkById(@Param("id_medecin")Long id_medecin,@Param("id_follower") Long id_follower) throws Exception;


}
