package com.clinitalPlatform.services.interfaces;


import java.util.List;
import java.util.Optional;

import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.CabinetRequest;

public interface CabinetService {
	 Optional<Cabinet>   findById(Long id) throws Exception;
	 List<Cabinet> findByName(String name) throws Exception;
	 List<Cabinet> findAll() throws Exception;
	 Cabinet create(CabinetRequest cabinetreq,Medecin med) throws Exception;
	 List<Cabinet> allCabinetsByMedID(Long id) throws Exception;



}
