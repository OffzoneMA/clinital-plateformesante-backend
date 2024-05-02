package com.clinitalPlatform.services.interfaces;


import com.clinitalPlatform.models.Langue;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Tarif;

import java.util.List;

public interface MedecinService {
	
	 Medecin findById(Long id) throws Exception;
	 Medecin getMedecinByUserId(long id) throws Exception;

	 //Recuperation de langués de medecins
	List<Langue> getLanguesByMedecinId(Long medecinId) throws Exception;
	List<Langue> getLanguesByMedecinName(String nomMed) throws Exception;

	List<Tarif>getTarifByMedecinId(Long medecinId)throws Exception;

	List<Tarif>getTarifByMedecinName(String nomMed)throws Exception;




}
