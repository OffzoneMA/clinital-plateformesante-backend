package com.clinitalPlatform.services;

import com.clinitalPlatform.enums.ProviderEnum;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.security.jwt.ConfirmationToken;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static net.andreinc.mockneat.types.enums.StringType.ALPHA_NUMERIC;
import static net.andreinc.mockneat.types.enums.StringType.HEX;
import static net.andreinc.mockneat.unit.text.Strings.strings;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
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

import com.clinitalPlatform.enums.DemandeStateEnum;
import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.util.ApiError;
import com.clinitalPlatform.dto.DemandeDTO;
import com.clinitalPlatform.payload.response.MessageResponse;
import com.clinitalPlatform.services.interfaces.DemandeService;

@Transactional
@Service
public class DemandeServiceImpl implements DemandeService{
	
	@Autowired
	private DemandeRepository demandeRepository;

	@Autowired
	private AutService authService;
	
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
    @Autowired
    private SpecialiteRepository specialiteRepository;
    @Autowired
    private VilleRepository villeRepository;
    @Autowired
    private MedecinRepository medecinRepository;

	private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String DIGITS = "0123456789";
	private static final String SPECIAL = "@$!%*?&";
	private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
	private static final SecureRandom random = new SecureRandom();

	@Override
	public ResponseEntity<?> create(DemandeDTO demande) {
		try {
			LOGGER.info("Demande données: {}", demande);

			if (demande == null || demande.getMail() == null || demande.getMail().isEmpty()) {
				return ResponseEntity.badRequest().body(new MessageResponse("Error: L'email est requis"));
			}

			if (userRepository.existsByEmail(demande.getMail())) {
				return ResponseEntity.badRequest().body(new MessageResponse("Error: Email déjà utilisé !"));
			}

			Demande d = modelMapper.map(demande, Demande.class);
			Demande saved = demandeRepository.save(d);

			// Create à new User from the Demande
			User user = new User();
			user.setEmail(saved.getMail());
			user.setTelephone(saved.getPhonenumber() != null ? saved.getPhonenumber() : "+212600000000");
			user.setRole(ERole.ROLE_MEDECIN);
			user.setProvider(ProviderEnum.LOCAL);
			User userSaved = userRepository.save(user);

			saved.setUser(userSaved);
			demandeRepository.save(saved);

			ConfirmationToken token = authService.createToken(user);

			//Envoie du mail de confirmation
			emailSenderService.sendMailConfirmation(user.getEmail(), token.getConfirmationToken());

			LOGGER.info("Demande d'inscription créée par un Médecin. Email : {}", d.getMail());
			return ResponseEntity.ok(modelMapper.map(saved, Demande.class));

		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de la demande: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new MessageResponse("Erreur interne, veuillez réessayer plus tard."));
		}
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
            LOGGER.info("Demande  has been Validated email : {}", demande.getMail());
			if(valide == DemandeStateEnum.VALIDER){
				User user = userRepository.findById(updated.getUser().getId()).orElseThrow(()->new Exception("this User is not found !"));
				String pw = this.generateSecurePassword(8);
				user.setPassword(passwordEncoder.encode(pw));
				User savedUser = userRepository.save(user);
				demande.setUser(user);

				//demande.setState(1);
				demandeRepository.save(demande);
                LOGGER.info("New User is Created, Email : {}", demande.getMail());
				userservice.save(savedUser , demande);

				// Send email to the new Medecin
				emailSenderService.sendMailDemandeValidation(demande,pw);

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

	public String generateSecurePassword(int length) {
		if (length < 8) {
			throw new IllegalArgumentException("Le mot de passe doit avoir au moins 8 caractères.");
		}

		List<Character> password = new ArrayList<>();

		// Ajout obligatoire de chaque type
		password.add(UPPER.charAt(random.nextInt(UPPER.length())));
		password.add(LOWER.charAt(random.nextInt(LOWER.length())));
		password.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
		password.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

		// Remplissage du reste avec des caractères aléatoires
		for (int i = 4; i < length; i++) {
			password.add(ALL.charAt(random.nextInt(ALL.length())));
		}

		// Mélange
		Collections.shuffle(password, random);

		// Conversion en chaîne
		StringBuilder sb = new StringBuilder();
		for (char c : password) {
			sb.append(c);
		}

		return sb.toString();
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