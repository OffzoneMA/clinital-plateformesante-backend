package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.ModeConsultation;
import com.clinitalPlatform.models.Rendezvous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartRepository extends JpaRepository<Rendezvous,Long> {

    /*@Query("SELECT r.modeConsultation.mode, COUNT(r) " +
            "FROM Rendezvous r " +
            "GROUP BY r.modeConsultation.mode")
    List<Object[]> countRendezvousByMode();*/
    @Query("SELECT r.modeConsultation.mode, YEAR(r.start), MONTH(r.start), COUNT(r) " +
            "FROM Rendezvous r " +
            "GROUP BY r.modeConsultation.mode, YEAR(r.start), MONTH(r.start)")
    List<Object[]> countRendezvousByModeAndMonthYear();

    @Query("SELECT r.modeConsultation.mode, YEAR(r.start), MONTH(r.start), COUNT(r) " +
            "FROM Rendezvous r " +
            "WHERE YEAR(r.start) = :year AND MONTH(r.start) = :month " +
            "GROUP BY r.modeConsultation.mode, YEAR(r.start), MONTH(r.start)")
    List<Object[]> countRendezvousByModeAndMonthYear(
            @Param("year") int year,
            @Param("month") int month
    );


}
