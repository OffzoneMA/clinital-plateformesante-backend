package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Langue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LangueRepository extends JpaRepository<Langue,Long> {

}
