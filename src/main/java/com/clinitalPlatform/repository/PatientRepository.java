package com.clinitalPlatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Patient;

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
}
