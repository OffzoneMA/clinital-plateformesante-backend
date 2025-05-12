package com.clinitalPlatform.repository;

import com.clinitalPlatform.enums.AntecedentTypeEnum;
import com.clinitalPlatform.models.Antecedents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AntecedentsRepository extends JpaRepository<Antecedents, Long> {

    @Query("SELECT a FROM Antecedents a WHERE a.dossier.id_dossier = :dossier")
    List<Antecedents> findByDossierId(Long dossier);

    @Query("SELECT a FROM Antecedents a WHERE a.dossier.id_dossier = :dossierId AND a.type = :type")
    Optional<Antecedents> findByDossierIdAndType(@Param("dossierId") Long dossierId, @Param("type") AntecedentTypeEnum type);
}
