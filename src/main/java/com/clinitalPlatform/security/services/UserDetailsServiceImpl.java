package com.clinitalPlatform.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.UserRepository;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Optional<User> userInfo = userRepository.findUserByEmail(username);

        return userInfo.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));
    }


  @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User" + id));

        return UserDetailsImpl.build(user);
    }


    @Transactional
    public Boolean isAccountVerified(String email) {
        return userRepository.findEmailVerifiedByEmail(email);
    }

    /*@Transactional
    /*public Boolean isEnabled(String email) {
        Boolean isEnabled = userRepository.findIsEnabledByEmail(email);
        return isEnabled;
    }*/

    public boolean isEnabled(String email) {
        return userRepository.findIsEnabledByEmail(email).orElse(false);
    }



}
