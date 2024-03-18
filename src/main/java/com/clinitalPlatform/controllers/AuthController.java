package com.clinitalPlatform.controllers;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@PostMapping("/signin")
    public ResponseEntity<?> authenticateAndGetToken( @RequestBody LoginRequest loginRequest) {
		System.out.println(loginRequest);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();
        String jwt=jwtService.generateToken(loginRequest.getEmail());
        User user = userServices.findById(userDetails.getId());
        globalVariables.setConnectedUser(user);
        if(userDetails.isEnabled()==false){
			return ResponseEntity.ok("Your Account is Blocked please try to Contact Clinital Admin");
		}
        	
        if (user.getEmailVerified() == true) {
        	autService.updateLastLoginDate(userDetails.getId());
        	activityServices.createActivity(new Date(), "Login", "Authentication reussi", user);
        	LOGGER.info("Authentication reussi");
			 return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(),
			 		userDetails.getTelephone(), userDetails.getRole()));
			
		} else {
			return ResponseEntity.ok(new ApiResponse(false, "Email Not Verified"));
		}
    }
	
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser( @RequestBody SignupRequest signUpRequest) throws Exception {

		System.out.println(signUpRequest);
		
		userServices.RegistreNewUser(signUpRequest);
		return ResponseEntity.ok(new ApiResponse(true, "User hhhhh registered successfully"));
			
	}
	
	@GetMapping("confirmaccount")
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
		
	}
	
}
