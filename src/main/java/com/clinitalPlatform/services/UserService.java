package com.clinitalPlatform.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.clinitalPlatform.enums.ProviderEnum;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.SignupRequest;
import com.clinitalPlatform.payload.response.MessageResponse;
import com.clinitalPlatform.repository.UserRepository;

@Service
public class UserService {

		@Autowired
		PasswordEncoder encoder;
	
	    @Autowired
	    private UserRepository userRepository;
	    
	    @Autowired
		ActivityServices activityServices;
	    
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
	   
		public ResponseEntity<?> RegistreNewUser(SignupRequest signUpRequest) throws Exception {

			if (userRepository.existsByEmail(signUpRequest.getEmail())) {
				return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
			}
			User user = new User(signUpRequest.getEmail(), signUpRequest.getTelephone(),
					encoder.encode(signUpRequest.getPassword()), signUpRequest.getRole());

			user.setProvider(ProviderEnum.LOCAL);
			user.setEnabled(true);
			// save user
			userRepository.save(user);

			activityServices.createActivity(new Date(), "add", "signup seccussefuly done", user);
			LOGGER.info("Inscription reussi");
			return ResponseEntity.ok(user);

		}
}
