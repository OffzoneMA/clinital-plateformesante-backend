package com.clinitalPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinitalPlatform.models.EmailConfirmationCode ;

public interface EmailConfirmationCodeRepository  extends JpaRepository<EmailConfirmationCode , Long>{
	EmailConfirmationCode findByCode(String code);
}
