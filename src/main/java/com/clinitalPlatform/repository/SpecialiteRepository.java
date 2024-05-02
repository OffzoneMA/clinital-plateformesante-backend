package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.Specialite;

import java.util.List;


@Repository
public interface SpecialiteRepository extends JpaRepository<Specialite, Long>{


    @Query(value = "SELECT * FROM `specialites` WHERE id_spec=?1",nativeQuery = true)
    Specialite getSpecialiteById(Long idspec);

    @Query(value = "SELECT * FROM `specialites` WHERE libelle LIKE ?1%",nativeQuery = true)
    Specialite getSpecialiteByName(String name);

}
