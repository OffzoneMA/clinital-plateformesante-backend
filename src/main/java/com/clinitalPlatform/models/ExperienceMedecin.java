package com.clinitalPlatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "experience_medecin")
@Data
public class ExperienceMedecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom_experience;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date_debut;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date_fin;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    @JsonIgnore
    private Medecin medecin;

    public ExperienceMedecin() {
    }

    public ExperienceMedecin(Long id, String nom_experience, Date date_debut, Date date_fin, Medecin medecin) {
        this.id = id;
        this.nom_experience = nom_experience;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.medecin = medecin;
    }
}
