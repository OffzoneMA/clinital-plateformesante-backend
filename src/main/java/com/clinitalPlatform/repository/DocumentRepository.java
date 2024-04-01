package com.clinitalPlatform.repository;


import com.clinitalPlatform.models.Document;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {


	@Query("from Document d where d.patient.nom_pat= ?1")
	List<Document> getDocByNomPatient(String nom_pat);


	List<Document> findAll();

	List<Document> findByPatientId(Long patientId);

	List<Document> findByPatientIdIn(List<Long> patientId);

	List<Document> findByPatientIdAndArchived(Long patientId, Boolean archived);
	
	List<Document> findByDossier(DossierMedical dossier);

	@Query("from Document d where d.rendezvous.id= ?1")
	List<Document> getDocByIdRendezvous(Long rdvId);

	@Query(value = "SELECT documents.* FROM documents, document_medecin dm WHERE documents.id_doc=dm.document_id AND documents.patient_id= ?1", nativeQuery = true)
	List<Document> getDocByPatientIdAndMedecin(Long patientId);

	@Query(value = "SELECT d.* FROM documents d, patients p WHERE d.patient_id=p.id AND p.patient_type='PROCHE' AND p.user_id= ?1", nativeQuery = true)
	List<Document> getDocPatientPROCH(Long patientId);
	@Query(value = "SELECT d.* FROM documents d, patients p WHERE d.patient_id=p.id AND p.patient_type='MOI' AND d.patient_id= ?1", nativeQuery = true)
	List<Document> getDocPatientMOI(Long patientId);

    @Query(value = "SELECT d.* FROM documents d WHERE d.id_dossier=?1", nativeQuery = true)
	List<Document> findByIdDossier(long iddoss);

	@Query(value = "SELECT p.* FROM patients p WHERE p.user_id= ?1 AND p.patient_type='PROCHE'", nativeQuery = true)
	List<Patient> getMesProches(Long connectedUserId);

}
