package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.FermetureExceptionnelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FermetureExceptionnelleRepository extends JpaRepository<FermetureExceptionnelle, Long> {
    List<FermetureExceptionnelle> findByMedecinIdAndDateFinAfter(Long medecin_id, LocalDateTime dateFin);

    List<FermetureExceptionnelle> findByMedecinIdAndDateDebutAfter(Long medecin_id, LocalDateTime dateDebut);

    List<FermetureExceptionnelle> findByMedecinIdAndDateDebutBeforeAndDateFinAfter(Long medecin_id, LocalDateTime dateDebut, LocalDateTime dateFin);

    List<FermetureExceptionnelle> findAllByMedecinId(Long medecinId);

    boolean existsByMedecinIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            Long medecinId,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    @Query("SELECT f FROM FermetureExceptionnelle f JOIN f.medecin m WHERE m.id = :medecinId AND f.dateDebut <= :dayEnd AND f.dateFin >= :dayStart")
    List<FermetureExceptionnelle> findFermeturesForMedecinAndDay(@Param("medecinId") Long medecinId,
                                                                 @Param("dayStart") LocalDate dayStart,
                                                                 @Param("dayEnd") LocalDate dayEnd);

}

