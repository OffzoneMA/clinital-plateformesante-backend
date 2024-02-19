package com.clinitalPlatform.models;

import java.time.LocalDate;

import com.clinitalPlatform.enums.AntecedentTypeEnum;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "anticedents")
@Data
public class Antecedents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_anticedent;

    private AntecedentTypeEnum type;

    private String descreption;

    private LocalDate date;

    @ManyToOne(cascade = CascadeType.ALL)
	private DossierMedical dossier;
    
}
