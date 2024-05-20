package com.clinitalPlatform.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinitalPlatform.repository.SecretaireRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import com.clinitalPlatform.repository.SpecialiteRepository;
import com.clinitalPlatform.enums.PatientTypeEnum;
import com.clinitalPlatform.models.Demande;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.Secretaire;
import com.clinitalPlatform.models.Specialite;
import com.clinitalPlatform.models.Ville;
import com.clinitalPlatform.util.ApiError;
import com.clinitalPlatform.enums.ProviderEnum;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.SignupRequest;
import com.clinitalPlatform.payload.response.MessageResponse;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.jwt.ConfirmationToken;

@Service
public class UserService {

		@Autowired
		PasswordEncoder encoder;
	
	    @Autowired
	    private UserRepository userRepository;
	    
	    @Autowired
		SecretaireRepository Secretairepos;
	    
	    @Autowired
		private ClinitalModelMapper mapper;
	    
	    @Autowired
		private PatientRepository patientRepository;
	    
	    @Autowired
		private MedecinRepository medecinrepo;
	    
	    @Autowired
		private DossierMedicalRepository dossierMedicalRepository;
	    
	    @Autowired
		ActivityServices activityServices;
	    
	    @Autowired
		SpecialiteRepository specialiterepo;
	    
	    @Autowired
		EmailSenderService emailSenderService;
	    
	    @Autowired
		private AutService authService;
	    
	    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	    
		public User findById(Long id) {
		        return userRepository.findById(id).get();        
		    }
	    public User findByEmail(String email) {
	        return userRepository.findByEmail(email);        
	    }
	    
	    public boolean existsByEmail(String email) {
	        return userRepository.existsByEmail(email);
	    }


	public User save(User user, Object obj) {

			try {

				switch (user.getRole()) {
					case ROLE_PATIENT:
						// save patient with dossier medical automatically
						Ville ville = new Ville();
						ville.setId_ville(1L);
						// generer un dossier medical par defaut
						DossierMedical dossierMedical = new DossierMedical();
						dossierMedical.setAlchole(false);
						dossierMedical.setFumeur(false);
						dossierMedical.setAccesscode(null);
						dossierMedical.setDossierType(null);
						dossierMedical.setNumDossier(null);
						dossierMedical.setTraitement(true);
						dossierMedicalRepository.save(dossierMedical);//sauvergarde du dossier medicale
						// generer un patient par defaut
						Patient patient = new Patient();
						patient.setNom_pat(null);
						patient.setPrenom_pat(null);
						patient.setDateNaissance(null);
						patient.setAdresse_pat(null);
						patient.setCodePost_pat(null);
						patient.setMatricule_pat(null);
						patient.setCivilite_pat(null);
						patient.setPatientEmail(user.getEmail());
						patient.setVille(ville);
						patient.setDossierMedical(dossierMedical);
						patient.setPatient_type(PatientTypeEnum.MOI);
						patient.setUser(user);
						patientRepository.save(patient); // sauvergarde des infos du patiens
						LOGGER.info("New Patient has been add ");
						break;
					case ROLE_MEDECIN:
						Demande demande = mapper.map(obj, Demande.class);
						Specialite specialite=specialiterepo.getSpecialiteByName(demande.getSpecialite());
						Medecin medecin = new Medecin();
						medecin.setNom_med(demande.getNom_med());
						medecin.setPrenom_med(demande.getPrenom_med());
						medecin.setInpe(demande.getInpe());
						medecin.setPhoto_med(null);
						medecin.setPhoto_couverture_med(null);
						medecin.setDescription_med(null);
						medecin.setContact_urgence_med(null);
						medecin.setCivilite_med(demande.getCivilite_med());
						medecin.setUser(user);
						medecin.setIsActive(false);
						medecin.setDiplome_med(null);
						medecin.setVille(null);
						medecin.setSpecialite(specialite);
						medecin.setStepsValidation(1L);
						medecinrepo.save(medecin);//Sauvergarde des infos du Médecins
						LOGGER.info("New Medecin has been add ");
						break;
					case ROLE_SECRETAIRE:
						Secretaire secrit = new Secretaire();
						secrit.setNom(null);
						secrit.setPrenom(null);
						secrit.setAdresse(null);
						secrit.setDateNaissance(null);
						secrit.setUser(user);
						secrit.setCabinet(null);
						Secretairepos.save(secrit); //Sauvegarde des infos sur la sécretaire
						LOGGER.info("New Secetaire has been add ");

					default:
						break;
				}

				ConfirmationToken token = authService.createToken(user);

				//Envoie du mail de confirmation
				emailSenderService.sendMailConfirmation(user.getEmail(), token.getConfirmationToken());
				//
				LOGGER.info("User informations has been Add seccessfully");
				return user;

			} catch (Exception e) {
				ResponseEntity.ok(new ApiError(false, "Error :" + e));
			}
			return null;

		}

	public ResponseEntity<?> registerNewUser(SignupRequest signUpRequest) {
		try {

			if (userRepository.existsByEmail(signUpRequest.getEmail())) {
				return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
			}

			User user = new User(signUpRequest.getEmail(), signUpRequest.getTelephone(),
					encoder.encode(signUpRequest.getPassword()), signUpRequest.getRole());

			user.setProvider(ProviderEnum.LOCAL);
			// Sauvergade d'un nouveau user
			userRepository.save(user);
			save(user, null); // Appel de la méthode save pour enregistrer l'utilisateur

			activityServices.createActivity(new Date(), "add", "signup seccussefuly done", user);
			LOGGER.info("Inscription reussi");
			return ResponseEntity.ok(user);
		} catch (Exception e) {
			LOGGER.error("Error occurred while registering new user: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(false, "An error occurred while registering new user."));
		}
	}
	 public boolean changePassword(User user, String password) {
	    	
			user.setPassword(encoder.encode(password));
			if (userRepository.save(user) != null) {
				emailSenderService.sendMailChangePassword(user.getEmail());
				return true;
				
			}
			return false;
		}

}
