package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Allergies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    
    @ManyToOne(cascade = CascadeType.ALL)
	private DossierMedical dossier;

    public Allergies(String name){
        super();
        this.name=name;
    }
}
