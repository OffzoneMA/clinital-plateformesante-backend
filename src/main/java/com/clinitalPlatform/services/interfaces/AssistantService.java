package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.models.Assistant;
import com.clinitalPlatform.models.Secretaire;
import com.clinitalPlatform.payload.request.SecritaireRequest;

import java.util.List;

public interface AssistantService {
	
//	 Assistant create(SecritaireRequest dto);
//
//	 Secretaire update(SecritaireRequest dto, Long id) throws Exception;
	
	 List<Assistant> findAll();
	
	 Assistant findById(Long id) throws Exception;

//	 boolean deleteSecretaireById(Long id,long cabinet) throws Exception;


}
