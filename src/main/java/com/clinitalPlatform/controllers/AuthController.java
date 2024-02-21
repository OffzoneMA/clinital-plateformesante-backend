package com.clinitalPlatform.controllers;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.util.GlobalVariables;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.LoginRequest;
import com.clinitalPlatform.payload.request.SignupRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.JwtResponse;
import com.clinitalPlatform.security.config.UserInfoUserDetails;
import com.clinitalPlatform.security.jwt.JwtService;
import com.clinitalPlatform.services.AutService;
import com.clinitalPlatform.services.UserService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	  private JwtService jwtService;
	
	@Autowired
	UserService userServices;
	 
	@Autowired
	AutService autService;
	
	@Autowired
    private GlobalVariables globalVariables;
	
	@Autowired
	ActivityServices activityServices;
	
	@PostMapping("/signin")
    public ResponseEntity<?> authenticateAndGetToken( @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserInfoUserDetails userDetails=(UserInfoUserDetails) authentication.getPrincipal();
        String jwt=jwtService.generateToken(loginRequest.getEmail());
        User user = userServices.findById(userDetails.getId());
        globalVariables.setConnectedUser(user);
        if(userDetails.isEnabled()==false){
			return ResponseEntity.ok("Your Account is Blocked please try to Contact Clinital Admin");
		}
        if (user.getEmailVerified() == true) {

        	autService.updateLastLoginDate(userDetails.getId());
        	activityServices.createActivity(new Date(), "Login", "Authentication reussi", user);

			 return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(),
			 		userDetails.getTelephone(), userDetails.getRole()));
			
		} else {
			return ResponseEntity.ok(new ApiResponse(false, "Email Not Verified"));
		}
    }
	
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser( @RequestBody SignupRequest signUpRequest) throws Exception {
		
		userServices.RegistreNewUser(signUpRequest);
		return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
			
	}
}
