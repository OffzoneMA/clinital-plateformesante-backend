package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "medecin_documents")
@Data
public class DocumentMedecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_doc;
    private Long numero_doc;
    private String titre_doc;
    private Date date_ajout_doc;

    private String fichier_doc;

    @Column(name = "date_modif_doc", nullable = true )
    private Date date_modif_doc = new Date();

    @Column(name = "archived", nullable = true, columnDefinition = "tinyint(1) default 0")
    private Boolean archived;

    @PrePersist
    protected void onCreate() {
        this.date_ajout_doc = new Date();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecinAuteur;

    @ManyToOne
    @JoinColumn(name = "type_doc", nullable = false, referencedColumnName = "id_typedoc", insertable = true, updatable = true)
    private TypeDocument typeDoc;

    @Column(name = "categorie", nullable = true)
    private String categorie = "medecin-docs";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doc_medecin_partage_medecins",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "medecin_id")
    )
    private List<Medecin> medecinsPartages;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doc_medecin_partage_patients",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private List<Patient> patientsPartages;

    @PreUpdate
    protected void onUpdate() {
        this.date_modif_doc = new Date();
    }

}
