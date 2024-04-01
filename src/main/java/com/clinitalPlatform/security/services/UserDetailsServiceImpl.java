package com.clinitalPlatform.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.UserRepository;

<<<<<<< HEAD
import jakarta.transaction.Transactional;
=======
>>>>>>> 99085ea3f9b1233061d1e0ed0b85ffba46361418
import java.util.Optional;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
<<<<<<< HEAD
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



=======
    private UserRepository repository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userInfo = repository.findUserByEmail(username);
        return userInfo.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));
    }
>>>>>>> 99085ea3f9b1233061d1e0ed0b85ffba46361418
}
