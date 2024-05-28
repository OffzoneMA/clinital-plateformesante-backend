package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.MoyenPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoyenPaiementRepository extends JpaRepository<MoyenPaiement,Long> {

}
