package com.clinitalPlatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinitalPlatform.models.EmailConfirmationCode ;

public interface EmailConfirmationCodeRepository  extends JpaRepository<EmailConfirmationCode , Long>{
	EmailConfirmationCode findByCode(String code);
	List<EmailConfirmationCode> findByUserId(Long userId);
}
