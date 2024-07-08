package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.ModeConsultation;
import com.clinitalPlatform.models.Rendezvous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartRepository extends JpaRepository<Rendezvous,Long> {

    @Query("SELECT r.modeConsultation.mode, COUNT(r) " +
            "FROM Rendezvous r " +
            "GROUP BY r.modeConsultation.mode")
    List<Object[]> countRendezvousByMode();

}
