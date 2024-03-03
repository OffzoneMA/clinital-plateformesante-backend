package com.clinitalPlatform.repository;

import com.clinital.models.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDemandeRepository extends JpaRepository<Demande, Long>{

    
    @Query(value="SELECT d.* FROM demande d WHERE d.validation=?1",nativeQuery = true)
    List<Demande> getdemandeByState(String state);
    
    @Query(value="SELECT d.* FROM demande d WHERE d.id=?1",nativeQuery = true)
    Optional<Demande> findByIDemande(Long id);

    @Query(value="SELECT d.* FROM demande d WHERE d.id=?1",nativeQuery = true)
    Demande findByid(Long id);

}
