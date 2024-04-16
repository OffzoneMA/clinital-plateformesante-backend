package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.dto.MedecinDTO;
import com.clinitalPlatform.dto.PatientDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.MedecinRequest;

import java.util.List;

public interface MedecinService {
	
	MedecinDTO create(MedecinRequest dto) throws Exception;
	
	 MedecinDTO update(MedecinRequest dto, Long id) throws Exception;
	
	 List<MedecinDTO> findAll();
	
	 Medecin findById(Long id) throws Exception;

	 void deleteById(Long id) throws Exception;
	 
	List<PatientDTO> getMedecinPatients(Long id) throws Exception;

	PatientDTO getPatient(Long idmed, long idpat) throws Exception;
	Medecin getMedecinByUserId(long id) throws Exception;


}
