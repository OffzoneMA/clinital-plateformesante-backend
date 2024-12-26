package com.clinitalPlatform.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.clinitalPlatform.dto.UserDTO;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.services.*;
import com.clinitalPlatform.util.GlobalVariables;

import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.clinitalPlatform.models.EmailConfirmationCode;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.LoginRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.MessageResponse;
import com.clinitalPlatform.repository.ConfirmationTokenRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.jwt.ConfirmationToken;
import org.springframework.http.HttpStatus;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users/")
public class UserController {

	@Autowired
	private UserService userservice;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	ActivityServices activityServices;

	@Autowired
	GlobalVariables globalVariables;
	
	@Autowired
	PatientService patientService;
	
	@Autowired
	private EmailConfirmationService confirmationService;
	
	@Autowired
	EmailSenderService emailSenderService;
	@Autowired
	ConfirmationTokenRepository confirmationTokenRepository;

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	// A method that resets the password of a user. %ok%
	@PostMapping("/respw")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT','ROLE_SECRETAIRE','ROLE_MEDECIN')")
	public ResponseEntity<?> resetPassword(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
		
		User user = userRepository.getById(globalVariables.getConnectedUser().getId());

		if (userservice.changePassword(user, loginRequest.getPassword())) {

			activityServices.createActivity(new Date(), "Update", "Password changed successfully",
					globalVariables.getConnectedUser());
			LOGGER.info("Password changed successfully, UserID : " + globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
		} else {
			return ResponseEntity.badRequest().body(new MessageResponse("Unable to change password. Try again!"));
		}

	}
	@PostMapping("/sendconfirmationcode")
	@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public ResponseEntity<?> enableUserWithConfirmation(@Valid @RequestBody String enableUserEmail) {
	    User user = userRepository.findByEmail(enableUserEmail);
	    if (user == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found"));
	    }

	    String confirmationCode = confirmationService.generateConfirmationCode();
	    confirmationService.saveConfirmationCode(user, confirmationCode);

	    emailSenderService.sendMailConfirmationCode(enableUserEmail, confirmationCode);

	    return ResponseEntity.ok(new ApiResponse(true, "Confirmation code sent successfully"));
	}
	
	@PostMapping("/supprimercompte")
	@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public ResponseEntity<?> enableUserWithConfirmationCode(@Valid @RequestBody String code) {
	    EmailConfirmationCode confirmationCode = confirmationService.findByCode(code);
	    if (confirmationCode == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Invalid confirmation code"));
	    }
	   
	    User user = confirmationCode.getUser();
	    confirmationService.deleteConfirmationCode(user.getId());
	    activityServices.deleteActivitiesByUserId(user.getId());
	    patientService.setUserNullByUserId(user.getId()); 
	    List<ConfirmationToken> tokensToDelete = confirmationTokenRepository.findByUserId(user.getId());
	    
	    // Supprimer toutes les instances récupérées
	    confirmationTokenRepository.deleteAll(tokensToDelete);
	    userRepository.delete(user);
	    
	    return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
	}

	@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	@PostMapping("/updateme")
	public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UserUpdateRequest request) {
		try {
			// Récupérer l'utilisateur connecté
			User currentUser = userRepository.findById(globalVariables.getConnectedUser().getId())
					.orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

			// Vérification si l'email a changé
			boolean emailChanged = !currentUser.getEmail().equals(request.getEmail());

			// Mise à jour des informations
			currentUser.setEmail(request.getEmail());
			currentUser.setTelephone(request.getTelephone());

			// Sauvegarder les modifications
			User user = userRepository.save(currentUser);

			// Envoyer un email de notification si l'adresse email a changé
			if (emailChanged) {
				emailSenderService.sendEmailChangeNotification(currentUser.getEmail(), request.getEmail());
			}

			// Retourner la réponse
			Map<String, Object> response = new HashMap<>();
			response.put("user", user);
			response.put("emailChanged", emailChanged);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Erreur lors de la mise à jour des informations utilisateur"));
		}
	}

	@GetMapping("/me")
	public ResponseEntity<UserUpdateRequest> getConnectedUserInfo() throws NotFoundException {
		Long userId = globalVariables.getConnectedUser().getId();
		User currentUser = userRepository.findById(userId)
				.orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

		UserUpdateRequest response = new UserUpdateRequest();
		response.setEmail(currentUser.getEmail());
		response.setTelephone(currentUser.getTelephone());
		return ResponseEntity.ok(response);
	}


}
