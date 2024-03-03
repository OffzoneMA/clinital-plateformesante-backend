package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Document;
import com.clinitalPlatform.models.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

	@Query("from Document d where d.patient.id= ?1")
	List<Document> getDocByIdPatient(Long id_patient);

	@Query("from Document d where d.patient.nom_pat= ?1")
	List<Document> getDocByNomPatient(String nom_pat);

	public List<Document> findAll();

	@Query("from Document d where d.id= ?1")
	List<Document> getDocumentById(Long id);

	List<Document> findByPatientId(Long patientId);

	List<Document> findByPatientIdIn(List<Long> patientsId);

	List<Document> findByPatientIdAndArchived(Long patientId, Boolean archived);
	
	List<Document> findByDossier(DossierMedical dossier);

	@Query("from Document d where d.rendezvous.id= ?1")
	List<Document> getDocByIdRendezvous(Long rdvId);

	@Query(value = "SELECT documents.* FROM documents  , document_medecin dm WHERE documents.id_doc=dm.document_id AND documents.patient_id IN(?1)", nativeQuery = true)
	List<Document> getDocByPatientIdAndMedecin(List<Long> patientsId);

	@Query(value = "SELECT d.* FROM documents d, patients p WHERE d.patient_id=p.id AND p.patient_type='PROCH' AND d.patient_id IN(?1)", nativeQuery = true)
	List<Document> getDocPatientPROCH(List<Long> patientsId);
	@Query(value = "SELECT d.* FROM documents d, patients p WHERE d.patient_id=p.id AND p.patient_type='MOI' AND d.patient_id IN(?1)", nativeQuery = true)
	List<Document> getDocPatientMOI(List<Long> patientsId);
//	@Query(value = "SELECT * FROM documents WHERE ", nativeQuery = true)
//	List<Document> getAllDocumentsByPatientId(Long patientId);

    @Query(value = "SELECT d.* FROM documents d WHERE d.id_dossier=?1", nativeQuery = true)
	List<Document> findByIdDossier(long iddoss);

}
