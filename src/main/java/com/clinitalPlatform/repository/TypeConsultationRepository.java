package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.TypeConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TypeConsultationRepository extends JpaRepository<TypeConsultation, Long> {

@Query(value = "SELECT c.* FROM type_consultation c WHERE medecin_id= :medecinId",nativeQuery = true)
	List<TypeConsultation> findAllByMedecinId(Long medecinId);

	@Query(value = "SELECT c.* FROM type_consultation c WHERE c.consultation_id= :idSchedule", nativeQuery = true)
	TypeConsultation findTypeConsultationById(@Param("idSchedule")Long idSchedule) throws Exception;

	@Modifying
	@Query(value = "DELETE FROM type_consultation WHERE consultation_id = :idtype and medecin_id=:idMed", nativeQuery = true)
	void DeletTypeConsultByMedId(@Param("idMed")Long idMed,@Param("idtype")Long idtype) throws Exception;


}
