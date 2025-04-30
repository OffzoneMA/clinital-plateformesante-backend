package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    //Metadonnees utiles
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
