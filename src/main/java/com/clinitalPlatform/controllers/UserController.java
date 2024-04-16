package com.clinitalPlatform.controllers;

import java.util.Date;

import javax.validation.Valid;

import com.clinitalPlatform.util.GlobalVariables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.models.EmailConfirmationCode;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.LoginRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.MessageResponse;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.services.UserDetailsServiceImpl;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.EmailConfirmationService;
import com.clinitalPlatform.services.EmailSenderService;
import com.clinitalPlatform.services.UserService;
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
	private EmailConfirmationService confirmationService;
	
	@Autowired
	EmailSenderService emailSenderService;

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
	    user.setEnabled(false);
	    userRepository.save(user);

	    confirmationService.deleteConfirmationCode(confirmationCode);

	    return ResponseEntity.ok(new ApiResponse(true, "User enabled successfully"));
	}

	
}
