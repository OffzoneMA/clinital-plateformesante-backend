package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "allergies")
@Data
public class Allergies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    
    @ManyToOne(cascade = CascadeType.ALL)
	private DossierMedical dossier;
    public Allergies(){
        super();
    }

    public Allergies(String name){
        super();
        this.name=name;
    }
}
