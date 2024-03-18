package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Long> {

}

