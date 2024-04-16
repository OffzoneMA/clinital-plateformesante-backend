package com.clinitalPlatform.controllers;

import java.util.*;

import com.clinitalPlatform.security.services.UserDetailsServiceImpl;
import com.clinitalPlatform.services.EmailSenderService;
import com.clinitalPlatform.util.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.clinitalPlatform.repository.UserRepository;
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
	
	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder encoder;
	
	//Jounalisation
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


	// A method that is called when the user clicks on the link in the email.%OK%
	@PostMapping("/signin")
    public ResponseEntity<?> authenticateAndGetToken( @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();
        String jwt=jwtService.generateToken(loginRequest.getEmail());
        User user = userServices.findById(userDetails.getId());
        globalVariables.setConnectedUser(user);
        if(!userDetails.isEnabled()){
			return ResponseEntity.ok("Your Account is Blocked please try to Contact Clinital Admin");
		}
        	
        if (user.getEmailVerified()) {
        	autService.updateLastLoginDate(userDetails.getId());
        	activityServices.createActivity(new Date(), "Login", "Authentication reussi", user);
        	LOGGER.info("Authentication reussi");
			 return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(),
			 		userDetails.getTelephone(), userDetails.getRole()));
			
		} else {
			return ResponseEntity.ok(new ApiResponse(false, "Email Not Verified"));
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

/*@GetMapping("confirmaccount")
	public ResponseEntity<?> getMethodName(@RequestParam String token) {

		ConfirmationToken confirmationToken = autService.findByConfirmationToken(token);

		if (confirmationToken == null) {
			throw new BadRequestException("Invalid token");
		}

		User user = confirmationToken.getUser();
		Calendar calendar = Calendar.getInstance();

		if ((confirmationToken.getExpiryDate().getTime() - calendar.getTime().getTime()) <= 0) {
			return ResponseEntity.badRequest()
					.body("Lien expiré, generez un nouveau lien http://localhost:8080/signin");
		}

		user.setEmailVerified(true);
		user.setEnabled(true);
		autService.save(user);
		LOGGER.info("Account verified successfully :"+user.getEmail());
		return ResponseEntity.ok("this Account verified successfully!");

	}*/



	@GetMapping("confirmaccount")
	public ResponseEntity<?> confirmAccount(@RequestParam String token) {
		ConfirmationToken confirmationToken = autService.findByConfirmationToken(token);

		if (confirmationToken == null) {
			throw new BadRequestException("Token invalide");
		}

		User user = confirmationToken.getUser();
		Calendar calendar = Calendar.getInstance();

		if ((confirmationToken.getExpiryDate().getTime() - calendar.getTime().getTime()) <= 0) {

			String newLink = "http://localhost:8080/api/auth/generateNewLink?token=" + token;
			return ResponseEntity.badRequest().body("Lien expiré, générer un nouveau " + newLink);
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

		return ResponseEntity.ok(newLink);
	}




	@GetMapping("/checkToken/{token}")
	public Boolean verifierValiditeToken(@PathVariable String token) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(token));
		return jwtService.validateToken(token, userDetails);
	}

	@PostMapping("/checkPassword")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	public ResponseEntity<?> checkPassword(@Valid @RequestBody String password)throws Exception {
		User user = userRepository.getById(globalVariables.getConnectedUser().getId());
	    
	    // Vérifier si le mot de passe fourni correspond au mot de passe de l'utilisateur connecté
	    boolean passwordMatch = encoder.matches(password, user.getPassword());
	    
	    if (passwordMatch) {
	        return ResponseEntity.ok(new ApiResponse(true, "Password matches"));
	    } else {
	        return ResponseEntity.ok(new ApiResponse(false, "Password does not match"));
	    }
	}
}
