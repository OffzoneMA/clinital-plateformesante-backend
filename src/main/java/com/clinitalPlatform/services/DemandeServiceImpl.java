package com.clinitalPlatform.services;

import org.springframework.transaction.annotation.Transactional;

import static net.andreinc.mockneat.types.enums.StringType.ALPHA_NUMERIC;
import static net.andreinc.mockneat.types.enums.StringType.HEX;
import static net.andreinc.mockneat.unit.text.Strings.strings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

import jakarta.persistence.EntityNotFoundException;

import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.enums.DemandeStateEnum;
import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.util.ApiError;
import com.clinitalPlatform.dto.DemandeDTO;
import com.clinitalPlatform.models.Demande;
import com.clinitalPlatform.payload.response.MessageResponse;
import com.clinitalPlatform.repository.DemandeRepository;
import com.clinitalPlatform.services.interfaces.DemandeService;

@Transactional
@Service
public class DemandeServiceImpl implements DemandeService{
	
	@Autowired
	private DemandeRepository demandeRepository;
	
	@Autowired
	private ClinitalModelMapper modelMapper;
	
	@Autowired
	EmailSenderService emailSenderService;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	GlobalVariables globalVariables;
	
	@Autowired
	UserService userservice;

	public final Logger LOGGER=LoggerFactory.getLogger(this.getClass());

	@Override
	public ResponseEntity<?> create(DemandeDTO demande) {

		if (userRepository.existsByEmail(demande.getMail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		Demande d = modelMapper.map(demande,Demande.class);
		Demande saved = demandeRepository.save(d);
	
        LOGGER.info("Demande d'inscription cree par un Medecin son email : "+d.getMail());
		return ResponseEntity.ok(modelMapper.map(saved, Demande.class));
	}
	
	@Override
	public DemandeDTO update(DemandeDTO demande, Long id) throws Exception {
		Optional<Demande> demandeOptional = demandeRepository.findByIDemande(id);
		if(demandeOptional.isPresent()) {
			Demande demande1 = modelMapper.map(demande, Demande.class);
			demande1.setId(id);
			Demande updated = demandeRepository.save(demande1);
		
        	LOGGER.info("Modification Demande  cree par un Medecin son email "+demande1.getMail());
			return modelMapper.map(updated, DemandeDTO.class);
		}else
			throw new Exception("Failed to update");
	}

	@Override
	public List<DemandeDTO> findAll() {
		LOGGER.info("Admin consult All Demandes");
		return demandeRepository.findAll()
				.stream()
				.map(dm->modelMapper.map(dm, DemandeDTO.class))
				.collect(Collectors.toList());
	}
	
	@Override
	public DemandeDTO findById(Long id) throws Exception { 
				Demande demande=demandeRepository.findById(id).orElseThrow(()->new Exception("NO matching Found"));
				LOGGER.info("Consult Demande By ID : "+id);
		return modelMapper.map(demande, DemandeDTO.class);
	}
	
	@Override
	public void deleteById(Long id) throws Exception {
		Optional<Demande> demande = demandeRepository.findByIDemande(id);	
		if(demande.isPresent()) {
			LOGGER.info("Demande  has been Deleted : "+demande.get().getMail());
			demandeRepository.deleteById(id);
		}
		else
			throw new Exception("Demande not found");
    }

	@Override
	public List<Demande> findByState(DemandeStateEnum state) {
		List<Demande> dmnd = demandeRepository.getdemandeByState(state.toString());
		LOGGER.info("Consulting Demandes by it State ");
		return dmnd;
	}
	
	@Override
	public Demande validate(DemandeStateEnum valide, Long id) throws Exception {
		
		try {	
			Optional<Demande> demandeOptional = demandeRepository.findByIDemande(id);
			
		if(demandeOptional.isPresent()) {

			Demande demande = demandeOptional.get();
			demande.setValidation(valide);
			Demande updated = demandeRepository.save(demande);
			LOGGER.info("Demande  has been Validated email : "+demande.getMail());
			if(valide==DemandeStateEnum.VALIDER){
				String pw=this.SecretCode();
				User user=new User();
				user.setEmail(demande.getMail());
				user.setPassword(passwordEncoder.encode(pw));
				user.setTelephone("0600000000");
				user.setRole(ERole.ROLE_MEDECIN);
				userRepository.save(user);
				demande.setUser(user);
				demande.setState(1);
				demandeRepository.save(demande);
				LOGGER.info("New User is Created, Email : "+demande.getMail());			
				User registred = userRepository.findById(user.getId()).orElseThrow(()->new Exception("this User is not found !"));
				userservice.save(registred,demande);
				emailSenderService.sendMailDemandeValidation(demande,pw);
				System.out.println(registred.getId());
			}
			return modelMapper.map(updated, Demande.class);
		}
			
		} catch (Exception e) {
			ResponseEntity.ok(new ApiError(false, " ERROR :"+e));
		}
		return null;	
	}

	public String SecretCode(){
		 String code = strings().size(10).types(ALPHA_NUMERIC, HEX).get();
		 return code;
      }
	
	 @Override
	 public Demande findDemandeByConnectedUser(Long userId) {
	        User user = userRepository.findById(userId)
	                .orElseThrow(() -> new EntityNotFoundException("User not found"));

	        return demandeRepository.findByUser(user)
	                .orElseThrow(() -> new EntityNotFoundException("Demande not found for the connected user"));
	    }
	 
	@Override
	public Demande updateDemandeStateByUserId(Long userId, int newState) throws EntityNotFoundException {
	  // Recherche de l'utilisateur par ID
	  User user = userRepository.findById(userId)
	          .orElseThrow(() -> new EntityNotFoundException("User not found"));
	
	  // Recherche de la demande associée à l'utilisateur
	  Demande demande = demandeRepository.findByUser(user)
	          .orElseThrow(() -> new EntityNotFoundException("Demande not found for the user"));
	
	  // Mise à jour de l'état de la demande
	  demande.setState(newState);
	
	  // Enregistrement de la demande mise à jour
	  return demandeRepository.save(demande);
	}
}