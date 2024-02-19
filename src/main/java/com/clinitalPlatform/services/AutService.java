package com.clinitalPlatform.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.UserRepository;

@Transactional
@Service
public class AutService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
	PasswordEncoder encoder;

	public void updateLastLoginDate(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + userId));

		user.setLastLogin(new Date());

		userRepository.save(user);
	}

}