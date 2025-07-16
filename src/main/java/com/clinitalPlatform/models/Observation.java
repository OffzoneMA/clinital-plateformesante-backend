package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "observations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private LocalDateTime dateObservation = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierMedical dossier;

    private String updatedBy;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onUpdate() {
        this.dateObservation = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdatePersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
