package com.clinitalPlatform.services;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinitalPlatform.models.EmailConfirmationCode;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.EmailConfirmationCodeRepository;

@Service
public class EmailConfirmationService {
    @Autowired
    private EmailConfirmationCodeRepository codeRepository;
    
    private static final int CODE_LENGTH = 6; // Longueur du code de confirmation
    private static final SecureRandom secureRandom = new SecureRandom();

    public String generateConfirmationCode() {
        byte[] randomBytes = new byte[CODE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public void saveConfirmationCode(User user, String code) {
        EmailConfirmationCode confirmationCode = new EmailConfirmationCode();
        confirmationCode.setUser(user);
        confirmationCode.setCode(code);
        codeRepository.save(confirmationCode);
    }

    public void deleteConfirmationCode(EmailConfirmationCode code) {
        codeRepository.delete(code);
    }

    public EmailConfirmationCode findByCode(String code) {
        return codeRepository.findByCode(code);
    }
}
