package com.clinitalPlatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medecin_images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedecinImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String description;
    private String type; // "PROFILE", "CABINET", "DIPLOME", etc.
    private LocalDateTime dateAjout;
    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    @JsonIgnore
    private Medecin medecin;

    public MedecinImage(String imageUrl, String type, String description, Medecin medecin) {
        this.imageUrl = imageUrl;
        this.type = type;
        this.description = description;
        this.medecin = medecin;
    }

}
