package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.DossierMedical;

import java.util.Optional;


@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {

    @Query(value="SELECT d.* FROM `dossiers` d WHERE d.id_dossier=?1",nativeQuery = true)
    DossierMedical findByDossierId(Long dossierId);


    @Query(value="SELECT d.* FROM `dossiers` d, `dossier_medecin` m WHERE d.id_dossier=m.dossier_id AND m.dossier_id=?2 AND m.medecin_id=?1",nativeQuery = true)
    Optional<DossierMedical> getdossierByIdandMedId(Long id_med, long iddoss);



}
