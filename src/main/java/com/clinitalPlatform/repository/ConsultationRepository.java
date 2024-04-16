package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long>{
	
	@Transactional
    @Modifying
    @Query(value = "DELETE FROM `consultations` c WHERE c.id_consultation=?1",nativeQuery = true)
    public static boolean deleteConsultation(Long idcons){
        return true;
    };

	@Query(value = "SELECT * FROM `consultations` c WHERE c.id_consultation=?1 AND c.id_patient=?2",nativeQuery = true)
	public Optional<Consultation> findByIdpatientandId(long id_consultation,Long id_pat);

	@Query(value = "SELECT c.* FROM `consultations` c WHERE c.id_consultation=?1 AND c.id_medecin=?2",nativeQuery = true)
	public Optional<Consultation> findByIdmedecinandId(long id_consultation,Long id_med);

	@Query(value = "SELECT * FROM `consultations` c WHERE c.id_patient=?1",nativeQuery = true)
	public List<Consultation> findAllByIdPatient(Long id_pat);

	@Query(value = "SELECT c.* FROM `consultations` c WHERE c.id_medecin=?1",nativeQuery = true)
	public List<Consultation> findAllByIdMedecin(Long id_med);

	@Query(value = "SELECT c.* FROM `consultations` c WHERE c.id_consultation=?1 AND c.id_medecin=?2",nativeQuery = true)
    Optional<Consultation> findIdandIdDossier(Long id,Long dossier);

	@Query(value = "SELECT c.* FROM `consultations` c WHERE c.id_consultation=?1",nativeQuery = true)
    	Optional<Consultation> findonebyAdmin(Long id);
	

}
