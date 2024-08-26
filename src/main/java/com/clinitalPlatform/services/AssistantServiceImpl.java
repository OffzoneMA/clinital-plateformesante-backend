package com.clinitalPlatform.services;

import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.Assistant;
import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.Secretaire;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.SecritaireRequest;
import com.clinitalPlatform.repository.AssistantRepository;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.SecretaireRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.services.interfaces.AssistantService;
import com.clinitalPlatform.services.interfaces.SecretaireService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class AssistantServiceImpl implements AssistantService {

	@Autowired
	private AssistantRepository assistantRepository;

	@Autowired
	private ClinitalModelMapper clinitalModelMapper;

	@Autowired
	private CabinetRepository cabinetrepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ClinitalModelMapper ModelMapper;

	@Autowired
    GlobalVariables globalVariables;

	@Autowired
	private ActivityServices ActivityServices;
	
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	





	@Override
	public List<Assistant> findAll() {
		try {ActivityServices.createActivity(new Date(), "Read", "Consult All Assistants ",globalVariables.getConnectedUser());
		
			LOGGER.info("Consult All Assistant, UserID : " + globalVariables.getConnectedUser().getId());
		
		return assistantRepository
				.findAll()
				.stream()
				.map(assistant->clinitalModelMapper.map(assistant, Assistant.class))
				.collect(Collectors.toList());} 
				catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Assistant findById(Long id) throws Exception {
	 Assistant assistant= assistantRepository.findById(id).orElseThrow(()-> new Exception("Assistant not found"));
	 ActivityServices.createActivity(new Date(), "Read", "Consult Assistant ID:"+id,globalVariables.getConnectedUser());
	LOGGER.info("Consult Assistant ID:"+id+", UserID : " + globalVariables.getConnectedUser().getId());
	 return assistant;
	}




	public List<Assistant> findByIdCabinet(Long id) {
		try {
			return assistantRepository
					.findAssistantsByCabinetId(id)
					.stream()
					.map(assistant->clinitalModelMapper.map(assistant, Assistant.class))
					.collect(Collectors.toList());}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
}