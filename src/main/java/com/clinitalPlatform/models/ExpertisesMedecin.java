package com.clinitalPlatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity(name = "expertises")
@Data

public class ExpertisesMedecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nom_exp;

    @ManyToMany
    @JsonIgnore
    private List<Medecin> medecins;

    public ExpertisesMedecin() {
    }

    public ExpertisesMedecin(Long id, String nom_exp, List<Medecin> medecins) {
        this.id = id;
        this.nom_exp = nom_exp;
        this.medecins = medecins;
    }
}
