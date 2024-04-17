package com.clinitalPlatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Medecin;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

	
	@Query(value = "SELECT m.* FROM medecins m WHERE m.user_id= :id",nativeQuery = true)
	public Medecin getMedecinByUserId(@Param("id")long id);
	
	@Query(value = "SELECT m.* FROM medecins m where m.nom_med = ?1 AND is_active = 1", nativeQuery = true)
	List<Medecin> getMedecinByName(String nom_med);
	
	@Query(value = "SELECT m.* FROM medecins m, villes v WHERE m.ville_id_ville = v.id_ville AND"
			+ " m.ville_id_ville = ?1 AND m.is_active = 1", nativeQuery = true)
	List<Medecin> getMedecinByVille(Long id_ville);
	
	 @Query(value = "SELECT m.* FROM medecins m, specialites s , villes v WHERE s.id_spec = m.specialite_id_spec AND "
	            + "m.ville_id_ville = v.id_ville AND m.is_active = 1 AND v.nom_ville = ?2 AND"
	            + " (s.libelle LIKE CONCAT(?1, '%') OR m.nom_med LIKE CONCAT(?1, '%'))", nativeQuery = true)
	    List<Medecin> getMedecinBySpecialiteOrNameAndVille(String search, String ville);
	
	 @Query(value = "SELECT m.* FROM medecins m, Specialites s WHERE s.id_spec = m.specialite_id_spec"
				+ " AND m.is_active = 1 AND s.libelle like CONCAT(?1, '%') and m.nom_med like CONCAT(?2, '%')", nativeQuery = true)
	List<Medecin> getMedecinBySpecialiteAndName(String specialite, String name);
	
	@Query(value = "SELECT m.* FROM medecins m, Specialites s WHERE s.id_spec = m.specialite_id_spec"
			+ " AND m.is_active = 1 AND (s.libelle like CONCAT(?1, '%') OR m.nom_med like CONCAT(?1, '%'))", nativeQuery = true)
	List<Medecin> getMedecinBySpecOrName(String search);

	//FILTRE
		@Query(value = "SELECT DISTINCT m.* FROM medecins m " +
				"INNER JOIN medecin_schedule ms ON m.id = ms.medecin_id " +
				"WHERE ms.day_of_week IN (1, 2, 3, 4, 5) AND m.is_active = 1", nativeQuery = true)
		List<Medecin> findMedecinsAvailableWeekdays();

		@Query(value = "SELECT DISTINCT m.* FROM medecins m " +
				"INNER JOIN medecin_schedule ms ON m.id = ms.medecin_id " +
				"WHERE ms.day_of_week IN (6, 7) AND m.is_active = 1", nativeQuery = true)
		List<Medecin> findMedecinsAvailableWeekend();

		@Query(value = "SELECT DISTINCT m.* FROM medecins m " +
				"INNER JOIN medecin_schedule ms ON m.id = ms.medecin_id " +
				"WHERE ms.day_of_week IN (:day1, :day2) AND m.is_active = 1", nativeQuery = true)
		List<Medecin> findMedecinsAvailableNextTwoDays(
				@Param("day1") int day1,
				@Param("day2") int day2
		);
}

