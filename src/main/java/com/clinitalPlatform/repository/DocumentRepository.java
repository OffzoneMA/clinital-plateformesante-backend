package com.clinitalPlatform.repository;


import com.clinitalPlatform.models.Document;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Patient;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {


	@Query("from Document d where d.patient.nom_pat= ?1")
	List<Document> getDocByNomPatient(String nom_pat);

	@Query("from Document d where d.patient.id= ?1")
	List<Document> getDocumentsByPatientId(Long id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE documents SET rdv_id = NULL WHERE rdv_id IN (SELECT id FROM rendezvous WHERE patient = ?1)", nativeQuery = true)
	void dissociateDocumentsByPatient(long patientId);

	@Modifying
	@Transactional
	@Query("UPDATE Document d SET d.patient = NULL WHERE d.patient.id = ?1")
	void dissociateDocumentsPaientByPatient(long patientId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM document_medecin WHERE document_id IN (SELECT d.id_doc FROM documents d WHERE d.patient_id = ?1)", nativeQuery = true)
	void deleteDocumentsMedecinsByPatientId(long patientId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM document_medecin WHERE document_id IN (SELECT d.id_doc FROM documents d WHERE d.rdv_id IN (SELECT r.id FROM rendezvous r WHERE r.patient = ?1))", nativeQuery = true)
	void deleteDocumentsMedecinsByRendezvousPatient(long patientId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM document_medecin WHERE document_id = :documentId", nativeQuery = true)
	void deleteByDocumentId(@Param("documentId") Long documentId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM documents WHERE patient_id = ?1", nativeQuery = true)
	void deleteDocumentsByPatientId(long patientId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM documents WHERE rdv_id IN (SELECT id FROM rendezvous WHERE patient = ?1)", nativeQuery = true)
	void deleteDocumentsByPatient(long patientId);

	List<Document> findAll();

	List<Document> findByPatientId(Long patientId);

	List<Document> findByPatientIdIn(List<Long> patientId);

	List<Document> findByPatientIdAndArchived(Long patientId, Boolean archived);
	
	List<Document> findByDossier(DossierMedical dossier);

	@Query("from Document d where d.rendezvous.id= ?1")
	List<Document> getDocByIdRendezvous(Long rdvId);

	@Query(value = "SELECT documents.* FROM documents, document_medecin dm WHERE documents.id_doc=dm.document_id AND documents.patient_id= ?1", nativeQuery = true)
	List<Document> getDocByPatientIdAndMedecin(Long patientId);

	/*@Query(value = "SELECT d.* FROM documents d, patients p WHERE d.patient_id=p.id AND p.patient_type='PROCHE' AND p.user_id= ?1", nativeQuery = true)
	List<Document> getDocPatientPROCH(Long patientId);*/
	@Query(value = "SELECT d FROM Document d JOIN FETCH d.patient p WHERE p.patient_type = 'PROCHE' AND p.user.id = ?1")
	List<Document> getDocPatientPROCH(Long userId);

	@Query(value = "SELECT d.* FROM documents d, patients p WHERE d.patient_id=p.id AND p.patient_type='MOI' AND d.patient_id= ?1", nativeQuery = true)
	List<Document> getDocPatientMOI(Long patientId);

    @Query(value = "SELECT d.* FROM documents d WHERE d.id_dossier=?1", nativeQuery = true)
	List<Document> findByIdDossier(long iddoss);

	@Query(value = "SELECT p FROM Patient p WHERE p.user.id = :userId")
	List<Patient> getMeAndMesProches(@Param("userId") Long userId);


	@Query(value = "SELECT d FROM Document d JOIN FETCH d.patient p WHERE p.patient_type = 'MOI' AND p.user.id = ?1")
	List<Document> getMyDocs(Long userId);

	@Query("SELECT COUNT(d) FROM Document d JOIN d.patient p WHERE p.id = ?1")
	Long countByPatientId(Long id);

}
