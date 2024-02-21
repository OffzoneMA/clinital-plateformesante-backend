package com.clinitalPlatform.services.interfaces;

import java.util.List;

import com.clinitalPlatform.dto.ActivityDTO;

public interface historyservices {
    
    ActivityDTO create(ActivityDTO dto);
	
	 ActivityDTO update(ActivityDTO dto, Long id) throws Exception;
	
	 List<ActivityDTO> findAll();
	
	 ActivityDTO getactivityByIdUser(Long id) throws Exception;

	 void deleteById(Long id) throws Exception;
}
