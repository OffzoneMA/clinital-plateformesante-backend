package com.clinitalPlatform.dto;

import lombok.Data;

@Data
public class ExperienceMedecinDTO {
    private Long id;
    private String nom_experience;
    private String etablissement;
    private String annee_debut;
    private String annee_fin;
    private boolean post_actuel = false;
    private Long emplacementId; // ID de Ville
    private Long medecinId = 0L; // ID de Medecin
}

