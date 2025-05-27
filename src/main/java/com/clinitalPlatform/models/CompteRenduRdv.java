package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteRenduRdv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column
    private String numero;

    @Column
    private String fichier_url;

    @Column
    private String nom_fichier;

    @ManyToOne
    @JoinColumn(name = "rdv_id", nullable = false)
    private Rendezvous rdv;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "date_ajout")
    private LocalDateTime date_ajout = LocalDateTime.now();

    @Column(name = "date_modification")
    private LocalDateTime date_modification;

    @PrePersist
    public void prePersist() {
        this.date_ajout = LocalDateTime.now();
        this.date_modification = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.date_modification = LocalDateTime.now();
    }
}
