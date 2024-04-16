package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Ordonnance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdonnanceRepository extends JpaRepository<Ordonnance,Long>  {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM `ordonnance` c WHERE c.id_ordon=?1",nativeQuery = true)
    public static boolean deleteOrdonnance(Long idord){
        return true;
    };

    @Query(value = "SELECT o.* FROM `ordonnance` o WHERE o.id_ordon=?1 AND o.dossier=?2",nativeQuery = true)
    Optional<Ordonnance> findIdandIdDossier(Long idord,Long iddoss);

    @Query(value = "SELECT o.* FROM `ordonnance` o WHERE o.id_ordon=?1 AND o.medecin=?2",nativeQuery = true)
    Optional<Ordonnance> findIdandIdMedecin(Long idord,Long med);

    @Query(value = "SELECT o.* FROM `ordonnance` o WHERE o.medecin=?1",nativeQuery = true)
    List<Ordonnance> findAllByIdMed(Long med);
}
