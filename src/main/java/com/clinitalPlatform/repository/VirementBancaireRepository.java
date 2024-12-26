package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.VirementBancaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirementBancaireRepository extends JpaRepository<VirementBancaire, Long> {

    @Query(value = "SELECT vb.* FROM virement_bancaire vb WHERE vb.id_medecin =?1 AND vb.id_mp =?2", nativeQuery = true)
    Optional<VirementBancaire> findByMedecinIdAndMoyenPaiementId_mp(long medecinId, long moyenPaiementId);

}

