package com.clinitalPlatform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedecinImageDTO {
    private Long id;
    private String imageUrl;
    private String description;
    private String type;
    private LocalDateTime dateAjout;
    private boolean isActive;
    private Long medecinId;

    // Constructeur pour la création
    public MedecinImageDTO(String imageUrl, String type, String description, Long medecinId) {
        this.imageUrl = imageUrl;
        this.type = type;
        this.description = description;
        this.medecinId = medecinId;
        this.dateAjout = LocalDateTime.now();
        this.isActive = false;
    }
}
