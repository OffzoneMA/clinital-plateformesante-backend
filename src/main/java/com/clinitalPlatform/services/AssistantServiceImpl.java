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
import javassist.NotFoundException;
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
	public List<Assistant> findAll() throws NotFoundException {
		User connectedUser = globalVariables.getConnectedUser();
		if (connectedUser == null) {
			throw new IllegalStateException("Aucun utilisateur connecté.");
		}

		List<Assistant> assistants = assistantRepository.findAll();
		for (Assistant assistant : assistants) {
			clinitalModelMapper.map(assistant, Assistant.class);
		}

		ActivityServices.createActivity(new Date(), "Read", "Consult All Assistants ", connectedUser);
		return assistants;
	}

	@Override
	public Assistant findById(Long id) throws NotFoundException {
		User connectedUser = globalVariables.getConnectedUser();
		if (connectedUser == null) {
			throw new IllegalStateException("Aucun utilisateur connecté.");
		}

		Optional<Assistant> assistantOptional = assistantRepository.findById(id);
		if (assistantOptional.isEmpty()) {
			throw new NotFoundException("Assistant non trouvé avec l'ID : " + id);
		}

		Assistant assistant = assistantOptional.get();
		ActivityServices.createActivity(new Date(), "Read", "Consult Assistant ID:" + id, connectedUser);

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