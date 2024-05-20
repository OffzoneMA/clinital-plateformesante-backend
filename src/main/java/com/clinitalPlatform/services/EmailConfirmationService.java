package com.clinitalPlatform.services;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

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

    public void deleteConfirmationCode(Long userId) {
        // Récupérer tous les codes de confirmation d'e-mail associés à l'utilisateur ayant user_id=id
        List<EmailConfirmationCode> codesToDelete = codeRepository.findByUserId(userId);
        
        // Supprimer tous les codes récupérés
        codeRepository.deleteAll(codesToDelete);
    }

    public EmailConfirmationCode findByCode(String code) {
        return codeRepository.findByCode(code);
    }
}
