package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.MedecinImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedecinImageRepository extends JpaRepository<MedecinImage, Long> {

    List<MedecinImage> findByMedecinId(Long medecinId);

    List<MedecinImage> findByMedecinIdAndType(Long medecinId, String type);

    Optional<MedecinImage> findByIdAndMedecinId(Long id, Long medecinId);

    @Query("SELECT mi FROM MedecinImage mi WHERE mi.medecin.id = :medecinId AND mi.type = :type AND mi.isActive = true")
    Optional<MedecinImage> findActiveImageByMedecinIdAndType(Long medecinId, String type);

    void deleteByMedecinId(Long medecinId);

    @Query("SELECT COUNT(mi) FROM MedecinImage mi WHERE mi.medecin.id = :medecinId AND mi.type = :type")
    int countByMedecinIdAndType(Long medecinId, String type);
}
