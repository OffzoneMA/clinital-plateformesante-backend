package com.clinitalPlatform.services.interfaces;

import java.util.List;

import com.clinitalPlatform.dto.SecretaireDTO;
import com.clinitalPlatform.models.Secretaire;
import com.clinitalPlatform.payload.request.SecritaireRequest;

public interface SecretaireService {
	
	 Secretaire create(SecritaireRequest dto);
	
	 Secretaire update(SecritaireRequest dto, Long id) throws Exception;
	
	 List<Secretaire> findAll();
	
	 Secretaire findById(Long id) throws Exception;

	 boolean deleteSecretaireById(Long id,long cabinet) throws Exception;


}
