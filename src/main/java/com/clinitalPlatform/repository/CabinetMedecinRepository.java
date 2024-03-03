package com.clinitalPlatform.repository;

import com.clinital.models.CabinetMedecinsSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CabinetMedecinRepository extends JpaRepository<CabinetMedecinsSpace,Long> {

    @Query(value = "SELECT * FROM cabinet_medecins WHERE medecin_id=?1 and cabinet_id=?2 AND status='ADMIN'",nativeQuery = true)
    Optional<CabinetMedecinsSpace> isAdmin(long id_med,long id_cab);

    @Modifying
    @Query(value="DELETE FROM cabinet_medecins WHERE cabinet_id=?1",nativeQuery=true)
    public void DeleteCabinetbyID(long Idcab);


    @Query(value = "SELECT * FROM cabinet_medecins WHERE medecin_id=?1 and cabinet_id=?2 AND status='ADMIN'",nativeQuery = true)
    public static Boolean isAllowed(long id_med,long id_cab){
        return true;
    }

    
}
