package com.clinitalPlatform.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SharingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_patient", nullable = true, referencedColumnName = "id_dossier", insertable = true, updatable = true)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "id_med", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
    private Medecin medecin;

    @ManyToOne
    @JoinColumn(name = "id_dossier", nullable = true, referencedColumnName = "id_dossier", insertable = true, updatable = true)
    private DossierMedical dossierMedical;

    private LocalDateTime dateshare;

    // Constructors, getters, and setters
    public SharingHistory(User user,Medecin medecin,Patient patient,DossierMedical dossierMedical,LocalDateTime dateshare ){
       this.user=user;
       this.patient=patient;
       this.medecin=medecin;
       this.dossierMedical=dossierMedical;
       this.dateshare=dateshare;
    }
}
