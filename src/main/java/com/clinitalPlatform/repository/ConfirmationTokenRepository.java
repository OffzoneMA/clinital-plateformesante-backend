package com.clinitalPlatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.security.jwt.ConfirmationToken;

@Repository
public interface ConfirmationTokenRepository 
            extends JpaRepository<ConfirmationToken, Long> {
    
    ConfirmationToken findByConfirmationToken(String token); 

    @Query(value = "", nativeQuery = true)
    ConfirmationToken getConfirmationTokenByUserId(long id);
    
    List<ConfirmationToken> findByUserId(Long userId);
}