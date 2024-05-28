package com.clinitalPlatform.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "diplome_medecin")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiplomeMedecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom_diplome;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date_debut;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date_fin;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;
}
