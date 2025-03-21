package com.clinitalPlatform.dto;

import lombok.Data;

@Data
public class ContactRequestDTO {
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String message;
    private String profil;
}
