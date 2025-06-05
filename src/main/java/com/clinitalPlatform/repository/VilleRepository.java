package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Ville;

@Repository
public interface VilleRepository extends JpaRepository<Ville, Long>{
    @Query("SELECT v FROM Ville v WHERE v.nom_ville = ?1")
    Ville findByNom_ville(String nom);
}

