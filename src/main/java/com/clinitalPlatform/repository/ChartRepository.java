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

    /*@Query("SELECT r.modeConsultation.mode, YEAR(r.start), MONTH(r.start), COUNT(r) " +
            "FROM Rendezvous r " +
            "WHERE YEAR(r.start) = :year AND MONTH(r.start) = :month " +
            "GROUP BY r.modeConsultation.mode, YEAR(r.start), MONTH(r.start)")
    List<Object[]> countRendezvousByModeAndMonthYear(
            @Param("year") int year,
            @Param("month") int month
    );*/
    @Query(value = "SELECT " +
            "SUM(CASE WHEN mc.mode_consultation = 'CABINET' THEN 1 ELSE 0 END) AS cabinetCount, " +
            "SUM(CASE WHEN mc.mode_consultation = 'VIDEO' THEN 1 ELSE 0 END) AS videoCount, " +
            "SUM(CASE WHEN mc.mode_consultation = 'DOMICILE' THEN 1 ELSE 0 END) AS domicileCount " +
            "FROM rendezvous r " +
            "JOIN mode_consultation mc ON r.id_mode = mc.id_mode " +
            "WHERE MONTH(r.start) = :month AND YEAR(r.start) = :year", nativeQuery = true)
    List<Object[]> countRendezvousByModeAndMonthYear(@Param("year") int year, @Param("month") int month);

    /*@Query(value = "SELECT " +
            "SUM(CASE WHEN p.civilite_pat = 'Mme' THEN 1 ELSE 0 END) AS femmeCount, " +
            "SUM(CASE WHEN p.civilite_pat = 'Mr' THEN 1 ELSE 0 END) AS hommeCount, " +
            "SUM(CASE WHEN p.date_naissance > DATE_SUB(CURRENT_DATE, INTERVAL 18 YEAR) THEN 1 ELSE 0 END) AS enfantCount " +
            "FROM patients p", nativeQuery = true)
    List<Object[]> getCountsByCiviliteAndAge();*/
    @Query(value = "SELECT " +
            "SUM(CASE WHEN p.civilite_pat = 'Mme' THEN 1 ELSE 0 END) AS femmeCount, " +
            "SUM(CASE WHEN p.civilite_pat = 'Mr' THEN 1 ELSE 0 END) AS hommeCount, " +
            "SUM(CASE WHEN p.date_naissance > DATE_SUB(CURRENT_DATE, INTERVAL 18 YEAR) THEN 1 ELSE 0 END) AS enfantCount " +
            "FROM patients p " +
            "JOIN rendezvous r ON p.id = r.patient " +
            "WHERE MONTH(r.start) = :month AND YEAR(r.start) = :year", nativeQuery = true)
    List<Object[]> getCountsByCiviliteAndAge(@Param("month") int month, @Param("year") int year);

}
