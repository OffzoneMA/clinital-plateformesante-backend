package com.clinitalPlatform.repository;

import java.util.List;

import com.clinitalPlatform.models.Langue;
import com.clinitalPlatform.models.MedecinNetwork;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
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
	
	 /*@Query(value = "SELECT m.* FROM medecins m, specialites s , villes v WHERE s.id_spec = m.specialite_id_spec AND "
	            + "m.ville_id_ville = v.id_ville AND m.is_active = 1 AND v.nom_ville = ?2 AND"
	            + " (s.libelle LIKE CONCAT(?1, '%') OR m.nom_med LIKE CONCAT(?1, '%'))", nativeQuery = true)
	    List<Medecin> getMedecinBySpecialiteOrNameAndVille(String search, String ville);*/

	@Query(value = "SELECT m.* FROM medecins m " +
			"JOIN specialites s ON s.id_spec = m.specialite_id_spec " +
			"JOIN villes v ON m.ville_id_ville = v.id_ville " +
			"LEFT JOIN cabinet_medecins cm ON m.id = cm.medecin_id " +
			"LEFT JOIN cabinet c ON cm.cabinet_id = c.id_cabinet " +
			"WHERE m.is_active = 1 " +
			"AND v.nom_ville = ?2 " +
			"AND (s.libelle LIKE CONCAT(?1, '%') " +
			"OR m.nom_med LIKE CONCAT(?1, '%') " +
			"OR c.nom LIKE CONCAT(?1, '%'))", nativeQuery = true)
	List<Medecin> getMedecinBySpecialiteOrNameOrCabinetAndVille(String search, String ville);


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

	@Query(value = "SELECT m.* FROM medecins m where m.nom_med = ?1 AND is_active = 1", nativeQuery = true)
	Optional<Medecin> findMedecinByName(String nom_med);

	@Query(value = "SELECT m.* FROM medecins m INNER JOIN cabinet_medecins cm ON m.id = cm.medecin_id INNER JOIN cabinet c ON cm.cabinet_id = c.id_cabinet WHERE c.nom = ?1", nativeQuery = true)
	public List<Medecin> getAllMedecinsByCabinetName(String nomCabinet);

	List<Medecin> findMedecinsByLangues_Name(String langueName);

//-----------------------------------------------NETWORK------------------
	@Query(value = "SELECT m.* FROM medecins m where id= ?1 AND is_active = 1", nativeQuery = true)
	Medecin getMedecinById(Long id);


	//	query to retrive all network
	@Query(value = "SELECT m.* FROM medecins m INNER JOIN medecin_network on m.id = medecin_network.id_follower WHERE medecin_network.id_medecin = ?1", nativeQuery = true)
	List<Medecin> getAllMedecinNetwork(long id_medecin) throws Exception;

	// query to retrive a network
	@Query(value = "SELECT m.* FROM medecins m INNER JOIN medecin_network on m.id = medecin_network.id_follower WHERE  medecin_network.id_medecin = :id_medecin AND medecin_network.id_follower= :id_follower", nativeQuery = true)
	Medecin getMedecinsFollowerByID(@Param("id_medecin")Long id_medecin,@Param("id_follower") Long id_follower) throws Exception;



}

