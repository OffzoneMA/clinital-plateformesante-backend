package com.clinitalPlatform.services.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.clinitalPlatform.models.Ordonnance;
import com.clinitalPlatform.payload.request.OrdonnanceRequest;
import com.clinitalPlatform.dto.OrdonnanceDTO;
import com.clinitalPlatform.models.Medecin;

public interface OrdonnanceServices {
	
	List<OrdonnanceDTO> findAllByMed(Medecin med) throws Exception;
	
	 OrdonnanceDTO findById(Long id,Medecin med) throws Exception;
	   
	 Boolean deleteById(Ordonnance consultation) throws Exception;
	 
	 List<OrdonnanceDTO> findAll() throws Exception;
	 
	 Ordonnance create(OrdonnanceRequest consultationRequest,Medecin med) throws Exception;
		
	 OrdonnanceDTO update(OrdonnanceRequest req,Medecin med) throws Exception;
	 
	 ResponseEntity<?> findByIdMedandDossierId(Medecin med,Long iddoss,Long idordo) throws Exception;
}




