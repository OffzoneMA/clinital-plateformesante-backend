package com.clinitalPlatform.repository;

import com.clinitalPlatform.security.jwt.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends
        JpaRepository<PasswordResetToken,Long> {


    PasswordResetToken findByResetToken(String resetToken);

    @Query(value = "", nativeQuery = true)
    PasswordResetToken findByUserId(long userId);

}
