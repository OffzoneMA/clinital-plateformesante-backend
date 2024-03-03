package com.clinitalPlatform.repository;

import com.clinital.security.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationTokenRepository 
            extends JpaRepository<ConfirmationToken, Long> {
    
    ConfirmationToken findByConfirmationToken(String token); 

    @Query(value = "", nativeQuery = true)
    ConfirmationToken getConfirmationTokenByUserId(long id);
}