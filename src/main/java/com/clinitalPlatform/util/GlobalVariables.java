package com.clinitalPlatform.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.services.UserDetailsImpl;

import javassist.NotFoundException;

@Component
public class GlobalVariables {

    @Autowired
    UserRepository userRepository;

    private User user;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
  
    // Get connected user
    public User getConnectedUser() throws NotFoundException {
        try {
        	 // Obtient le principal de l'authentification actuelle
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
            if (principal instanceof UserDetailsImpl) {
            	 // Si le principal est une instance de UserDetailsImpl, récupère les détails de l'utilisateur
            	UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            	 // Récupère l'utilisateur à partir de l'ID dans les détails de l'utilisateur
                return userRepository.getById(userDetails.getId());
            } else {
            	// Gère le cas où le principal n'est pas UserDetailsImpl
                throw new NotFoundException("Cannot find a matching user");
            }
        } catch (NotFoundException notFoundException) {
        	// Propage l'exception NotFoundException si elle est levée
            throw notFoundException;
        } catch (Exception e) {
        	// Journalise toute autre exception avec erreur et propage une exception RuntimeException
            LOGGER.error("Error fetching current user: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching current user");
        }
    }

    // Set connected user
    public void setConnectedUser(User user) {
       this.user=user;
    }

}