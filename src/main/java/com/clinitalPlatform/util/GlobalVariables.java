package com.clinitalPlatform.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.config.UserInfoUserDetails;

import javassist.NotFoundException;

@Component
public class GlobalVariables {

    @Autowired UserRepository userRepository;

    private User user;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
  

    public User getConnectedUser() throws NotFoundException {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
            if (principal instanceof UserInfoUserDetails) {
            	UserInfoUserDetails userDetails = (UserInfoUserDetails) principal;
                return userRepository.getById(userDetails.getId());
            } else {
                // Handle the case when the principal is not UserDetailsImpl
                throw new NotFoundException("Cannot find a matching user");
            }
        } catch (NotFoundException notFoundException) {
            throw notFoundException;
        } catch (Exception e) {
 
            LOGGER.error("Error fetching current user: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching current user");
        }
    }
    
    public void setConnectedUser(User user) {
       this.user=user;
    }
}