package com.clinitalPlatform.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ordonnance")
@Data
public class Ordonnance{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ordon;
    private LocalDate Date;
    private String details;

    @ManyToOne
    @JoinColumn(name = "medecin", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
    private Medecin medecin;

    @ManyToOne
    @JoinColumn(name = "dossier", nullable = true, referencedColumnName = "id_dossier", insertable = true, updatable = true)
    private DossierMedical dossier;

    @OneToOne(optional = true)
    @JoinColumn(name = "rdv_id", referencedColumnName= "id")
    private Rendezvous rendezvous;

    //Metadonnees utiles
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Ordonnance(){
        super();
    }
    public Ordonnance(String details,Medecin med,DossierMedical dossier,Rendezvous rendezvous){
        super();
        this.details=details;
        this.medecin=med;
        this.dossier=dossier;
        this.rendezvous=rendezvous;
    }

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