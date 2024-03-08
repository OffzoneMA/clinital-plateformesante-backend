package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.MedecinSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MedecinScheduleRepository extends JpaRepository<MedecinSchedule, Long> {

    @Query(value = "SELECT * FROM medecin_schedule WHERE medecin_id=?1",nativeQuery = true)
    List<MedecinSchedule> findByMedId(Long medecinId);

    @Query(value = "SELECT * FROM medecin_schedule WHERE medecin_id=?1",nativeQuery = true)
    List<MedecinSchedule> GetAllSchedulesByMedId(long idmed);
    @Query(value = "SELECT * FROM medecin_schedule WHERE id=?1",nativeQuery = true)
    MedecinSchedule GetSchedulesByIdsched(Long id);

    @Query(value ="SELECT * FROM medecin_schedule WHERE id = :id",nativeQuery = true)
    MedecinSchedule getById(Long id);

    @Query(value = "SELECT * FROM medecin_schedule WHERE medecin_id=?1 and type_consultation_id=?2",nativeQuery = true)
    List<MedecinSchedule> GetAllSchedulesByMedIdandIdConsult(long idmed,Long idconsult);

    @Modifying
    @Query(value="UPDATE medecin_schedule SET availability_end = ?1, availability_start = ?2, day = ?3, mode_consultation = ?4, period = ?5, medecin_id = ?6 WHERE id = ?7",nativeQuery = true)
    void UpdateSchedules(LocalDateTime end, LocalDateTime start, int day, String mode, String period, long Medid, long id);




}
