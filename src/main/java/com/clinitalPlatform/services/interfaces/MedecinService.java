package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.models.Medecin;

public interface MedecinService {
	
	 Medecin findById(Long id) throws Exception;
	 Medecin getMedecinByUserId(long id) throws Exception;
}
