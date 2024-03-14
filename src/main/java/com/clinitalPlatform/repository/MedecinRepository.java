package com.clinitalPlatform.repository;

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

//	Optional<Medecin> getMedByName(String nom_med);


    //	query to retrive all network
    @Query(value = "SELECT m.* FROM medecins m INNER JOIN medecin_network on m.id = medecin_network.id_follower WHERE medecin_network.id_medecin = ?1", nativeQuery = true)
    List<Medecin> getAllMedecinNetwork(long id_medecin) throws Exception;

    // query to retrive a network
    @Query(value = "SELECT m.* FROM medecins m INNER JOIN medecin_network on m.id = medecin_network.id_follower WHERE  medecin_network.id_medecin = :id_medecin AND medecin_network.id_follower= :id_follower", nativeQuery = true)
    Medecin getMedecinsFollowerByID(@Param("id_medecin")Long id_medecin,@Param("id_follower") Long id_follower) throws Exception;

    @Query(value = "SELECT m.* FROM from medecins m where m.nom_med = ?1 AND is_active = 1", nativeQuery = true)
    List<Medecin> getMedecinByName(String nom_med);

    @Query(value = "SELECT m.* FROM from medecins m where id= ?1 AND is_active = 1", nativeQuery = true)
    Medecin getMedecinById(Long id);

    @Query(value = "SELECT * FROM medecins where id= ?1 AND is_active = 1", nativeQuery = true)
    Optional<Medecin> findbyid(Long id);

    @Query(value ="SELECT * FROM medecins WHERE id = ?1",nativeQuery = true)
    Medecin getById(Long id);

    @Query(value = "SELECT m.* FROM medecins m, Specialites s , villes v WHERE s.id_spec = m.specialite_id_spec "
            + " AND m.ville_id_ville = v.id_ville AND m.is_active = 1  AND m.ville.nom_ville = '?2'"
            + " AND ( s.libelle LIKE ?1% OR m.nom_med LIKE ?1% )", nativeQuery = true)
    List<Medecin> getMedecinBySpecialiteOrName(String search, String ville);

    @Query(value = "SELECT m.* FROM medecins m, Specialites s , villes v WHERE s.id_spec = m.specialite_id_spec AND m.ville_id_ville = v.id_ville AND m.is_active = 1  AND m.ville_id_ville = 57 AND ( s.libelle LIKE ?1%  OR m.nom_med LIKE ?2% )", nativeQuery = true)
    List<Medecin> getMedecinBySpecialiteOrNameAndVille(String ville,String search);

    @Query(value = "SELECT m.* FROM medecins m, villes v WHERE m.ville_id_ville = v.id_ville AND m.ville_id_ville = ?1 AND m.is_active = 1", nativeQuery = true)
    List<Medecin> getMedecinByVille(Long id_ville);

    @Query(value = "SELECT m.* FROM medecins m, Specialites s WHERE s.id_spec = m.specialite_id_spec"
            + "AND m.is_active = 1 AND ( s.libelle like ?1%  OR m.nom_med like ?1%)", nativeQuery = true)
    List<Medecin> getMedecinBySpecOrName(String search);



    //



    // Query for getting all  medecins
    public List<Medecin> findAll();

    @Query(value = "SELECT m.* FROM medecins m WHERE m.user_id= :id",nativeQuery = true)
    public Medecin getMedecinByUserId(@Param("id")long id);

    @Modifying
    @Query(value = "UPDATE medecins SET is_active = ?1 WHERE id = ?2",nativeQuery = true)
    public void setVisibelityMedecin(boolean isActive,Long idmed);

}

