package com.clinitalPlatform.exception;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Custom access denied handler to handle access denied situations
public class CustumaccessdeniedHandler implements AccessDeniedHandler {
	// Logger for logging access denied events
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CustumaccessdeniedHandler.class);
    
    // Method to handle access denied situations
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
    	
    	// Retrieve authentication details of the current user
    	Authentication autentication = SecurityContextHolder.getContext().getAuthentication();
    	// Log access denied event including user details and requested URI
        LOG.info("Access denied for user " + autentication.getName()+" with roles "+autentication.getAuthorities()+" to "+request.getRequestURI());
        
    }
}
    

