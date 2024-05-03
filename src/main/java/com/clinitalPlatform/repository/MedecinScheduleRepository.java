package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.MedecinSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MedecinScheduleRepository extends JpaRepository<MedecinSchedule, Long> {

    @Query(value = "SELECT * FROM medecin_schedule WHERE medecin_id=?1",nativeQuery = true)
    List<MedecinSchedule> findByMedId(Long medecinId);

    @Query(value = "SELECT * FROM medecin_schedule WHERE medecin_id=?1",nativeQuery = true)
    List<MedecinSchedule> getAllSchedulesByMedId(long idmed);
    @Query(value = "SELECT * FROM medecin_schedule WHERE id=?1",nativeQuery = true)
    MedecinSchedule getSchedulesByIdsched(Long id);

    @Query(value ="SELECT * FROM medecin_schedule WHERE id = :id",nativeQuery = true)
    MedecinSchedule getById(Long id);
    //SELECT m.* FROM medecins m INNER JOIN medecin_network on m.id = medecin_network.id_follower WHERE  me
    @Query(value = "SELECT ms.* FROM medecin_schedule ms INNER JOIN modeschedules mc on ms.id=mc.sched_id WHERE ms.medecin_id=?1 and mc.mode_id=?2",nativeQuery = true)
    List<MedecinSchedule> getAllSchedulesByMedIdandIdConsult(long idmed,Long idconsult);

    @Modifying
    @Query(value="UPDATE medecin_schedule SET availability_end = ?1, availability_start = ?2, day = ?3, mode_consultation = ?4, period = ?5, medecin_id = ?6 WHERE id = ?7",nativeQuery = true)
    void updateSchedules(LocalDateTime end, LocalDateTime start, int day, String mode, String period, long Medid, long id);

    @Query(value = "SELECT DISTINCT * FROM medecin_schedule WHERE medecin_id=?1 ORDER BY availability_start, availability_end, DAYOFWEEK(day)", nativeQuery = true)
    List<MedecinSchedule> findByMedIdOrderByAvailability(Long medecinId);


    @Query(value = "SELECT DISTINCT * FROM medecin_schedule WHERE medecin_id = :medecinId " +
            "AND availability_start >= :startDate " +
            "AND availability_start < DATE_ADD(:startDate, INTERVAL :weeks WEEK) " +
            "ORDER BY availability_start, availability_end, DAYOFWEEK(day)",
            nativeQuery = true)
    List<MedecinSchedule> findByMedIdAndStartDateAndWeeksOrderByAvailability(
            @Param("medecinId") Long medecinId,
            @Param("startDate") LocalDate startDate,
            @Param("weeks") long weeks
    );

    @Query(value = "SELECT * FROM medecin_schedule WHERE medecin_id=:medecinId and day=:day",nativeQuery = true)
    List<MedecinSchedule> findByMedIdAndDay( @Param("medecinId")Long medecinId,
                                            @Param("day") Integer day);


}
