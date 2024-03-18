package com.clinitalPlatform.repository;


import com.clinitalPlatform.models.Rendezvous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RdvRepository extends JpaRepository<Rendezvous, Long> {

	// get Rdv By Day :
	@Query(value = "SELECT r.* FROM Rendezvous r , patients p where r.patient=p.id   and  DATE(r.start)= ?1 and p.user_id=?2",nativeQuery = true)
	List<Rendezvous> getRdvByDate(LocalDate day,long user_id);

	// @Query("from Rendezvous r where r.motif= ?1")
	// List<Rendezvous> getRdvByMotif(String motif);
	@Query(value ="SELECT * FROM rendezvous WHERE id = :id",nativeQuery = true)
	Rendezvous getById(Long id);
	

//RDV BY DATE (DAR,WEEK,MONTH,YEAR) For A DOCTOR :
	// // A method that returns a list of Rendezvous objects for a given day.
	@Query(value = "SELECT * FROM rendezvous WHERE day(start)=:day and medecin=:id", nativeQuery = true)
	List<Rendezvous> getRendezvousMedByDay(@Param(value = "day")long day, @Param(value = "id")long id);
	// A method that returns a list of Rendezvous objects for a given week.
	@Query(value = "SELECT * FROM rendezvous WHERE week(start)=:week and medecin=:id", nativeQuery = true)
	List<Rendezvous> getRendezvousMedByWeek(@Param(value = "week")long week, @Param(value = "id")long id);
	// A method that returns a list of Rendezvous objects for a given month.
	@Query(value = "SELECT * FROM rendezvous WHERE month(start)=:month and medecin=:id", nativeQuery = true)
	List<Rendezvous> getRendezvousMedByMonth(@Param(value = "month")long day, @Param(value = "id")long id);
	// A method that returns a list of Rendezvous objects for a given year.
	@Query(value = "SELECT * FROM rendezvous WHERE year(start)=:year and medecin=:id", nativeQuery = true)
	List<Rendezvous> getRendezvousMedByYear(@Param(value = "year")long day, @Param(value = "id")long id);

	
//RDV BY DATE (DAR,WEEK,MONTH,YEAR) For A Patient :
	// Get Rdv By day :
	@Query(value = "SELECT * FROM rendezvous WHERE day=:day and patient= :id", nativeQuery = true)
	List<Rendezvous> getRendezvousPatientByDayofweek(@Param(value = "day")long day, @Param(value = "id")long id);
	// Get Rdv By day :
	@Query(value = "SELECT * FROM rendezvous WHERE day(start)=:day and patient= :id", nativeQuery = true)
	List<Rendezvous> getRendezvousPatientByDay(@Param(value = "day")long day, @Param(value = "id")long id);

	@Query(value = "SELECT * FROM rendezvous WHERE week(start)=:week and patient= :id", nativeQuery = true)
	List<Rendezvous> getRendezvousPatientByWeek(@Param(value = "week")long week, @Param(value = "id")long id);

	@Query(value = "SELECT * FROM rendezvous WHERE month(start)=:month and patient= :id", nativeQuery = true)
	List<Rendezvous> getRendezvousPatientByMonth(@Param(value = "month")long week, @Param(value = "id")long id);

	@Query(value = "SELECT * FROM rendezvous WHERE year(start)=:year and patient= :id", nativeQuery = true)
	List<Rendezvous> getRendezvousPatientByYear(@Param(value = "year")long week, @Param(value = "id")long id);



	// get RDV by ID :
	@Query(value = "select * from Rendezvous  where id= :id", nativeQuery = true)
	Optional<Rendezvous> getRendezvousById(Long id);

	// get RDV by Iduser and id Patient :
	@Query(value = "SELECT r.* FROM rendezvous r , patients p WHERE r.patient= p.id and r.patient=?2 and p.user_id=?1", nativeQuery = true)
	List<Rendezvous> findRdvByIduserandPatient(Long iduser, Long patient);

	// get RDV by Id User and id Rdv :
	@Query(value = "SELECT r.* FROM rendezvous r , patients p WHERE r.patient= p.id and r.id=?2 and p.user_id=?1", nativeQuery = true)
	Rendezvous findRdvByIdUserandId(Long iduser, long Id);

	// get RDV by Id and id Medecin :
	@Query(value = "select * from rendezvous  where id = :id and medecin= :idmed", nativeQuery = true)
	Optional<Rendezvous> findRdvByIdandMedecin(Long id, long idmed);
	// get RDV by Id and id Patient :
	@Query(value = "select * from rendezvous  where id = :id and patient= :idpat", nativeQuery = true)
	Optional<Rendezvous> findRdvByIdandPatient(Long id, long idpat);
	// get RDV By id patient :
	@Query(value = "select * from rendezvous  where patient = ?1", nativeQuery = true)
	List<Rendezvous> getRdvByIdPatient(Long id);
	// get RDV By id Medecin :
	@Query(value = "select r.* from `rendezvous` r, `patients` p  where r.patient=p.id AND p.user_id=?2 AND r.medecin =?1", nativeQuery = true)
	List<Rendezvous> getRdvByIdMedecin(Long idmed,Long iduser);

	// get RDV by Id Patient Name : ROLE Patient
	@Query(value = "SELECT r.* FROM rendezvous r , patients p WHERE r.patient= p.id and p.nom_pat=?1 and p.user_id=?2", nativeQuery = true)
	List<Rendezvous> getRdvByNomPatient(String nom_pat, long IdUser);

	// get RDV by Patient Name : ROLE Medecin
	@Query(value = "SELECT r.* FROM rendezvous r , patients p WHERE r.patient= p.id and p.nom_pat=?1 and r.medecin=?2", nativeQuery = true)
	List<Rendezvous> getRdvByNomPatientByMedecin(String nom_pat, long IdMed);

	@Query("from Rendezvous r where r.day =?1 AND r.medecin.nom_med =?2")
	List<Rendezvous> findByDateandMedecin(Date day, String nom_med);

	@Query("select r from Rendezvous r where r.statut = 'CONFIRME' and r.patient.id =?1")
	List<Rendezvous> findConfirmedBypatientId(@Param("id") Long id);

	@Query("select r from Rendezvous r where r.statut = 'CONFIRME' and r.patient.nom_pat =?1")
	List<Rendezvous> findConfirmedBypatientId(@Param("nom_pat") String nom_pat);

	@Query(value ="SELECT * FROM `rendezvous` WHERE DATE(start)<=?1 AND DATE(end)>=?1 AND medecin=?2",nativeQuery=true)
	List<Rendezvous>   findByDateAndMedecin(LocalDate dayfrom, Long id);

	@Query(value = "select * from rendezvous where start =?1 AND patient =?2", nativeQuery = true)
	List<Rendezvous>   findByDateAndPatient(LocalDateTime dayFrom, Long id);

	@Query("from Rendezvous r where r.patient.id= ?1")
	public List<Rendezvous> findAll(Long id);

	@Query(value = "select * from rendezvous where medecin =?1", nativeQuery = true)
	List<Rendezvous> findByAllRdvByMedecin(Long id);

	@Query(value = "select * from rendezvous where patient =?1", nativeQuery = true)
	List<Rendezvous> findAllRdvByPatient(Long id);

	// List<Rendezvous> findByMedecinCabinetSecretairesId(Long id);

	@Modifying
	@Query(value = "DELETE FROM rendezvous WHERE id = :idrdv", nativeQuery = true)
	void DeletRdvByIdProfile(@Param("idrdv") Long idrdv) throws Exception;

	@Modifying
	@Query(value = "UPDATE rendezvous SET day = :#{#day_of_week.getValue()-1}, start = :start , statut = :statut, medecin = :medecinId, patient = :patientId, motif = :motif , end=:end,mode_consultation=:idmode WHERE id = :id", nativeQuery = true)
	void UpdateRdvByIdProfile(@Param("day_of_week")DayOfWeek day_of_week,@Param("start") LocalDateTime start,@Param("statut") String statut,@Param("medecinId") Long medecinId,
	@Param("patientId")Long patientId,@Param("motif") String motif,@Param("end") LocalDateTime end,@Param("id") long Id,@Param("idmode")long idmode) throws Exception;

	// get RDV by Id and id speciate in a date :
	@Query(value = "select p.* from rendezvous p , medecins m where p.medecin=m.id AND p.patient = :idpat and m.specialite_id_spec=:spec and DATE(p.start)=:date", nativeQuery = true)
	List<Rendezvous> findRdvByPatientandSpecInDate(@Param(value = "idpat")Long idpat,@Param(value = "spec")Long spec,@Param(value = "date")LocalDate date) throws Exception;

	// get RDV by Id and id Medecin :
	@Query(value = "select p.* from rendezvous p , medecins m where p.medecin=m.id AND  m.specialite_id_spec=:spec and DATE(start)=:date AND p.medecin=:idmed", nativeQuery = true)
	Rendezvous findRdvByMedcinandSpecInDate(@Param(value = "spec")long spec,@Param(value = "date")LocalDate date) throws Exception;

	@Query(value = "SELECT * from rendezvous WHERE start<=STR_TO_DATE(:datestart, '%Y-%m-%d %H:%i:%s') and end>STR_TO_DATE(:datestart, '%Y-%m-%d %H:%i:%s') AND day=:day AND medecin=:idmed", nativeQuery = true)
	Optional<Rendezvous> IsRdvinDateStart(@Param("datestart") LocalDateTime datestart ,@Param("day") int day,@Param("idmed") Long idmed) throws Exception;


	@Modifying
	@Query(value = "UPDATE rendezvous SET day = :#{#day.getValue()-1}, start = :start , end=:end WHERE id = :Idrdv", nativeQuery = true)
	void UpdateRdvdatestart(@Param("day")DayOfWeek day,@Param("start")LocalDateTime start,@Param("end")LocalDateTime end, @Param("Idrdv")long Idrdv) throws Exception;
	// Get rdv of the given med and pat from today and next comming
	@Query(value = "select * from rendezvous  where medecin=?1 and patient =?2 and DATE(start)>=curdate()", nativeQuery = true)
	List<Rendezvous> getRdvByIdMedecinandIdPatientandDate(Long idmed,Long patient);
}
