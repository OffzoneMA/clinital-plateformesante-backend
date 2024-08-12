package com.clinitalPlatform.repository;

import java.util.List;
import java.util.Optional;

import com.clinitalPlatform.enums.PatientTypeEnum;
import com.clinitalPlatform.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
  
	@Query("from Patient p where p.id = ?1")
	public Patient findPatientByAccount(Long userID) throws Exception;

	@Query(value = "SELECT p.* FROM patients p WHERE p.user_id=?1 AND p.id=?2",nativeQuery = true)
	Optional<Patient> getPatientByUserId(long id,long idpatient);

	// Deleting a patient from the database.
	@Modifying
	@Query(value="DELETE FROM patients WHERE id =?1 and patient_type='PROCHE'",nativeQuery = true)
	public void deletePatient(long id);
	
	@Query(value ="SELECT * FROM patients WHERE id = :id",nativeQuery = true)
	Patient getById(Long id);

	@Query(value = "SELECT p.* FROM patients p WHERE p.user_id=?1",nativeQuery = true)
	List<Patient> getPatientByUserId(long id);

	@Query(value = "SELECT p.* FROM patients p WHERE p.user_id=?1 AND patient_type='MOI'",nativeQuery = true)
	Patient getPatientMoiByUserId(long id);

	@Query(value = "SELECT p.* FROM patients p WHERE p.user_id=?1 AND patient_type='PROCHE'",nativeQuery = true)
	List<Patient> findALLProchByUserId(long id);
		
	@Query(value = "SELECT * FROM patients p WHERE p.user_id=?1 AND p.id_dossier=?2",nativeQuery = true)
	Optional<Patient> findPatientByUserIdandDossMedicale(Long iduser,Long iddoss);

	List<Patient> findByUserId(Long userId);

   @Query("from Patient p where p.patient_type = ?1")
   public List<Patient> findPatientByType(PatientTypeEnum type);

   public List<Patient> findAllById(Long userID);

   // Query for getting all patients of a medecin
   @Query(value = "SELECT p.* FROM patients p , dossiers d, dossier_medecin dm WHERE p.id_dossier=d.id_dossier and d.id_dossier=dm.dossier_id AND dm.medecin_id = :id_med",nativeQuery = true)
   List<Patient> getMedecinPatients(long id_med);
   @Query(value = "SELECT DISTINCT  p.* FROM patients p , rendezvous r WHERE r.medecin=:id_med",nativeQuery = true)
   List<Patient> getallpatientofmed(long id_med);
   // Query for getting a patient of a medecin
   @Query(value="SELECT p.* FROM patients p , dossiers d, dossier_medecin dm WHERE p.id_dossier=d.id_dossier and d.id_dossier=dm.dossier_id AND dm.medecin_id = :id_med AND p.id = :id_patient",nativeQuery=true)
   Patient getPatient(long id_med, long id_patient);

   @Query(value = "SELECT * FROM patients WHERE user_id=?1 AND patient_type='PROCHE' AND id=?2",nativeQuery = true)
   Patient findProchByUserId(long userid,long idpatient);

   @Query(value = "SELECT p.* FROM patients p WHERE p.user_id=?1",nativeQuery = true)
   List<Patient> findALLPatientByUserId(long id);

   @Query(value = "SELECT p.* FROM patients p WHERE p.user_id=?1",nativeQuery = true)
   Optional<Patient> findPatientByUserId(long id);

        @Query(value = "SELECT p.* FROM patients p WHERE p.id_dossier=?1",nativeQuery = true)
        Optional<Patient> findPatientByDossMedicale(Long iddoss);



       Optional<Patient> findByPatientEmail(String patientEmail);

    @Query(value = "SELECT p.* FROM patients p WHERE p.user_id = :id AND p.patient_type = 'MOI'", nativeQuery = true)
    Optional<Patient> findByUserIdAndPatientType(@Param("id") Long id);

}



 



