package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class FermetureExceptionnelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "motif_fermeture_exceptionnelle",
            joinColumns = @JoinColumn(name = "fermeture_id"),
            inverseJoinColumns = @JoinColumn(name = "motif_id"))
    private List<MotifFermeture> motifFermeture;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medecin_id", nullable = false, referencedColumnName = "id", insertable = true, updatable = true)
    private Medecin medecin;

    // Getters & Setters
}
