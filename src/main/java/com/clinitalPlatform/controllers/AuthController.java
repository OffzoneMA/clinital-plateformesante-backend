package com.clinitalPlatform.controllers;

import java.util.*;


import com.clinitalPlatform.payload.request.VerifyEmailRequest;
import com.clinitalPlatform.security.services.UserDetailsServiceImpl;
import com.clinitalPlatform.services.EmailSenderService;
import com.clinitalPlatform.util.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clinitalPlatform.util.GlobalVariables;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.LoginRequest;
import com.clinitalPlatform.payload.request.SignupRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.JwtResponse;
import com.clinitalPlatform.security.jwt.ConfirmationToken;
import com.clinitalPlatform.security.jwt.JwtService;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.AutService;
import com.clinitalPlatform.services.UserService;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	ActivityServices  activityServices;
	
	@Autowired
	UserService userServices;
	 
	@Autowired
	AutService autService;
	
	@Autowired
    private GlobalVariables globalVariables;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	@Autowired
	EmailSenderService emailSenderService;
	//Jounalisation
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());



	@PostMapping("/signin")
	public ResponseEntity<?> authenticateAndGetToken(@RequestBody LoginRequest loginRequest) {
		try {
			// Vérifier si le compte existe (via le mail)
			UserDetails userDetail = userDetailsService.loadUserByUsername(loginRequest.getEmail());

			// Vérifier si le compte est activé
			User useractif = userServices.findByEmail(loginRequest.getEmail());
			if (useractif != null && !useractif.getEmailVerified()) {
				LOGGER.info("Email is not verified");
				return ResponseEntity.ok(new ApiResponse(false, "Email Not Verified"));
			}
			// Vérifier si le compte est actif (non bloqué)
			if (!userDetailsService.isEnabled(loginRequest.getEmail())) {
				LOGGER.info("Account is blocked");
				return ResponseEntity.ok(new ApiResponse(false, "Your Account is Blocked please try to Contact Clinical Admin"));
			}

			// Générer le token JWT et effectuer la connexion
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
			String jwt = jwtService.generateToken(loginRequest.getEmail());
			User user = userServices.findById(userDetails.getId());
			System.out.println(user.getEmail());
			globalVariables.setConnectedUser(user);


			// Mettre à jour la date de dernière connexion et créer une activité de connexion
			autService.updateLastLoginDate(userDetails.getId());
			activityServices.createActivity(new Date(), "Login", "Authentication reussi", user);
			LOGGER.info("Authentication reussi");

			// Retourner la réponse avec le token JWT et les détails de l'utilisateur
			return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), userDetails.getTelephone(), userDetails.getRole()));
		} catch (UsernameNotFoundException e) {
			// Aucun compte associé à cet email
			System.out.println("no account");
			return ResponseEntity.ok(new ApiResponse(false, "no_account"));
		} catch (BadCredentialsException e) {
			System.out.println("incorrect");
			// Mot de passe incorrect
			return ResponseEntity.ok(new ApiResponse(false, "incorrect_password"));
		}
	}

	/*@PostMapping("/signup")
	public ResponseEntity<?> registerUser( @RequestBody SignupRequest signUpRequest) throws Exception {
		
		userServices.registerNewUser(signUpRequest);
		return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));

	}*/

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
		try {

			ResponseEntity<?> response = userServices.registerNewUser(signUpRequest);
			return response.getStatusCode().is2xxSuccessful()
					? ResponseEntity.ok(new ApiResponse(true, "User registered successfully"))
					: response;
		} catch (Exception e) {
			LOGGER.error("An error occurred during user registration: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiError(false, "An error occurred while registering new user."));
		}
	}


	@GetMapping("confirmaccount")
	public ResponseEntity<?> confirmAccount(@RequestParam String token) {
		ConfirmationToken confirmationToken = autService.findByConfirmationToken(token);

		if (confirmationToken == null) {
			throw new BadRequestException("Token invalide");
		}

		User user = confirmationToken.getUser();
		Calendar calendar = Calendar.getInstance();

		/*if (user.getEmailVerified()) {
			return ResponseEntity.badRequest().body("Votre compte est déjà activé.");
		}*/


		if ((confirmationToken.getExpiryDate().getTime() - calendar.getTime().getTime()) <= 0) {

			String newLink = "http://localhost:8080/api/auth/generateNewLink?token=" + token;
			return ResponseEntity.badRequest().body("Lien expiré, générer un nouveau <a href=\"" + newLink + "\">Ici</a>");

		}

		user.setEmailVerified(true);
		user.setEnabled(true);
		autService.save(user);
		LOGGER.info("Compte vérifié avec succès: " + user.getEmail());
		return ResponseEntity.ok("Le compte a été vérifié avec succès!");
	}

    //Generate a new link for the user

	@GetMapping("/generateNewLink")
	public ResponseEntity<String> generateNewLink(@RequestParam String token) {
		ConfirmationToken confirmationToken = autService.findByConfirmationToken(token);

		if (confirmationToken == null) {
			throw new BadRequestException("Invalid token");
		}

		User user = confirmationToken.getUser();

		// Vérifirier si le compte de l'utilisateur est déjà activé
		if (user.getEmailVerified()) {

			return ResponseEntity.badRequest().body("Votre compte est déjà activé.");
		}

		ConfirmationToken newToken = autService.createToken(user);

		String newLink = newToken.getConfirmationToken();


		emailSenderService.sendMailConfirmationNewlink(user.getEmail(), newLink);

		return ResponseEntity.ok("Un nouveau lien vous a été envoyer");
	}


	//recuperation d'un token de confirmation par user_id
	@GetMapping("/confirmationtoken/{userId}")
	public ResponseEntity<String> getConfirmationToken(@PathVariable Long userId) {

		ConfirmationToken confirmationToken = autService.getConfirmationTokenByUserId(userId);
		System.out.println("userid"+userId);
		if (confirmationToken == null) {
			return ResponseEntity.notFound().build();
		}

		String tokenValue = confirmationToken.getConfirmationToken();
		return ResponseEntity.ok(tokenValue);
	}



	@GetMapping("/checkToken/{token}")
	public Boolean verifierValiditeToken(@PathVariable String token) {

		UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(token));
		System.out.println("token verifié:" +token);
		return jwtService.validateToken(token, userDetails);

	}

	//Cas où un user veut demander un autre si il a egarer le premier
	@GetMapping("/newconfirmationLink")
	public ResponseEntity<?> sendVerificationMail(@Valid @RequestBody VerifyEmailRequest emailRequest) {

		if (autService.existsByEmail(emailRequest.getEmail())) {

			if (userDetailsService.isAccountVerified(emailRequest.getEmail())) {
				throw new BadRequestException("Email est déjà vérifié ");
			} else {
				User user = autService.findByEmail(emailRequest.getEmail());
				ConfirmationToken token = autService.createToken(user);

				emailSenderService.sendMail(user.getEmail(), token.getConfirmationToken());
				return ResponseEntity.ok(new ApiResponse(true, "Un lien de vérification a été envoyé par mail"));
			}
		} else {
			throw new BadRequestException("Email non associé ");
		}
	}



}
