package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.enums.DemandeStateEnum;
import com.clinitalPlatform.models.Demande;
import com.clinitalPlatform.dto.DemandeDTO;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

import org.springframework.http.ResponseEntity;


public interface DemandeService {
	
	ResponseEntity<?> create(DemandeDTO demande);
	
    DemandeDTO update(DemandeDTO demande,Long id) throws Exception;
	
	List<DemandeDTO> findAll();
	
	DemandeDTO findById(Long id) throws Exception;
	
	void deleteById(Long id) throws Exception;

	Demande validate(DemandeStateEnum valide,Long id) throws Exception;

    List<Demande> findByState(DemandeStateEnum state);
    
    Demande findDemandeByConnectedUser(Long id);
    
    Demande updateDemandeStateByUserId(Long userId, int newState) throws EntityNotFoundException;

}
