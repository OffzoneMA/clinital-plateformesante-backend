package com.clinitalPlatform.models;

import java.time.LocalDate;

import com.clinitalPlatform.enums.CabinetDocStateEnum;
import com.clinitalPlatform.enums.CabinetDocuemtsEnum;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="DocumentsCabinet")
@Data
public class DocumentsCabinet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private CabinetDocuemtsEnum type_doc;
    private LocalDate date_ajout_doc;
	private String fichier_doc;
    private LocalDate date_modif_doc;
    private String nom_fichier;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cabinet", nullable = false, referencedColumnName = "id_cabinet", insertable = true, updatable = true)
    private Cabinet cabinet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medecin", nullable = false)
    private Medecin medecin;


    private CabinetDocStateEnum validationState;

    public DocumentsCabinet(){
        super();
    }
    public DocumentsCabinet(CabinetDocuemtsEnum type,LocalDate date,String file, Cabinet cabinet,CabinetDocStateEnum state , Medecin medecin){
        super();
        this.type_doc=type;
        this.cabinet=cabinet;
        this.date_ajout_doc=date;
        this.fichier_doc=file;
        this.validationState=state;
        this.medecin = medecin;
    }

    @PrePersist
    protected void onCreate() {
        this.date_ajout_doc = LocalDate.now();
        this.date_modif_doc = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.date_modif_doc = LocalDate.now();
    }
}
