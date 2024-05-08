package com.clinitalPlatform.security.jwt;

import com.clinitalPlatform.models.JwtTokens;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.clinitalPlatform.security.services.UserDetailsServiceImpl;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Extract Authorization header which contains the JWT token
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract the token excluding "Bearer "
            token = authHeader.substring(7);
            // Extract username from the token
            username = jwtService.extractUsername(token);
        }
        // Check if username is extracted from the token and if the user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details by username
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the token against the loaded user details
            if (jwtService.validateToken(token, userDetails)) {

                // Create authentication token based on user details
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // Set authentication details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else if (jwtService.validateRefreshToken(token)) {
                // Generate a new access token using the existing generateTokens method
                JwtTokens jwtTokens = jwtService.generateTokens(username);
                // Redirect to a new URL with the new access token.
                String redirectUrl = request.getRequestURI() + "?access_token=" + jwtTokens.getAccessToken();
                response.sendRedirect(redirectUrl);
            }
        }
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

}
