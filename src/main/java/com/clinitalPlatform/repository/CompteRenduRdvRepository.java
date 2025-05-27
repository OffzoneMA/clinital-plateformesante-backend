package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.CompteRenduRdv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompteRenduRdvRepository extends JpaRepository<CompteRenduRdv, Long> {
    @Query("SELECT cr FROM CompteRenduRdv cr WHERE cr.medecin.id = :dossierId")
    List<CompteRenduRdv> findByMedecinId(Long dossierId);

    @Query("SELECT cr FROM CompteRenduRdv cr WHERE cr.rdv.id = :rdvId")
    List<CompteRenduRdv> findByRendezvousId(Long rdvId);

    @Query("SELECT cr FROM CompteRenduRdv cr WHERE cr.patient.id = :patientId")
    List<CompteRenduRdv> findByPatientId(Long patientId);

}
