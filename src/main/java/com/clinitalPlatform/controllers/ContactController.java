package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.ContactRequestDTO;
import com.clinitalPlatform.services.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private EmailSenderService mailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendContactForm(@RequestBody ContactRequestDTO contactRequest) {
        // Envoi du formulaire de contact au support
        mailService.sendContactForm(
                contactRequest.getEmail(),
                contactRequest.getPrenom(),
                contactRequest.getNom(),
                contactRequest.getTelephone(),
                contactRequest.getMessage(),
                contactRequest.getProfil()
        );

        // Envoi de l'email de confirmation à l'utilisateur
        mailService.sendContactConfirmation(contactRequest.getEmail(), contactRequest.getPrenom());

        return ResponseEntity.ok("Votre message a bien été envoyé et une confirmation a été envoyée à votre email.");
    }
}

