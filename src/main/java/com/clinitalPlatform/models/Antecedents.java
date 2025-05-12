package com.clinitalPlatform.models;

import java.time.LocalDate;

import com.clinitalPlatform.enums.AntecedentTypeEnum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "anticedents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Antecedents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_anticedent;

    private AntecedentTypeEnum type;

    private String descreption;

    private LocalDate date;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
	private DossierMedical dossier;
    
}
