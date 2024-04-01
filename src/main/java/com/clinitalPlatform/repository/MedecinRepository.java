package com.clinitalPlatform.repository;

<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> 99085ea3f9b1233061d1e0ed0b85ffba46361418
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Medecin;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
<<<<<<< HEAD
	@Query(value = "SELECT m.* FROM medecins m WHERE m.user_id= :id",nativeQuery = true)
	public Medecin getMedecinByUserId(@Param("id")long id);
}
=======
	
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
	
	//
}

>>>>>>> 99085ea3f9b1233061d1e0ed0b85ffba46361418
