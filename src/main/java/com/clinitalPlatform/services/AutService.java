package com.clinitalPlatform.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.ConfirmationTokenRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.jwt.ConfirmationToken;

@Transactional
@Service
public class AutService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;
    
	public void updateLastLoginDate(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + userId));

		user.setLastLogin(new Date());

		userRepository.save(user);
	}
	 public ConfirmationToken createToken(User user) {
	        ConfirmationToken confirmationToken = new ConfirmationToken(user);
	        return confirmationTokenRepository.save(confirmationToken);
	 }

	 
	 public User save(User user){
	       return userRepository.save(user);
	  }
	 public ConfirmationToken findByConfirmationToken(String token) {
	        return confirmationTokenRepository.findByConfirmationToken(token);
	    }

}