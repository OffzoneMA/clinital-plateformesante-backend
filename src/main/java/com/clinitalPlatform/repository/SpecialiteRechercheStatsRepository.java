package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.SpecialiteRechercheStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialiteRechercheStatsRepository extends JpaRepository<SpecialiteRechercheStats, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE SpecialiteRechercheStats s SET s.totalRecherches = s.totalRecherches + 1 WHERE s.id = :id")
    void incrementerRecherche(Long id);

    @Query("SELECT s FROM SpecialiteRechercheStats s WHERE s.specialite.id_spec = :specialiteId")
    Optional<SpecialiteRechercheStats> findBySpecialiteId(Long specialiteId);

    @Query("SELECT s FROM SpecialiteRechercheStats s JOIN FETCH s.specialite ORDER BY s.totalRecherches DESC")
    List<SpecialiteRechercheStats> findTopSpecialites(Pageable pageable);
}
