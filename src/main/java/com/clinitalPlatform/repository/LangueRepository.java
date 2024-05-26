package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Langue;
import com.clinitalPlatform.models.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LangueRepository extends JpaRepository<Langue,Long> {

}
