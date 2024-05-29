package com.clinitalPlatform.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@NoArgsConstructor
@ToString
@Data
public class SharingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ignore Hibernate proxy properties
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_patient", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ignore Hibernate proxy properties
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "id_med", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ignore Hibernate proxy properties
    private Medecin medecin;

    @ManyToOne
    @JoinColumn(name = "id_dossier", nullable = true, referencedColumnName = "id_dossier", insertable = true, updatable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ignore Hibernate proxy properties
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
