package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.CabinetStatuMedcinEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Cabinet_Medecins")
@Data
public class CabinetMedecinsSpace {

    @EmbeddedId
    private CabinetMedecinsIDs id;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @MapsId("medecin_id")
    @JoinColumn(name = "medecin_id", referencedColumnName = "id")
    @JsonIgnore
    private Medecin medecin;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @MapsId("cabinet_id")
    @JsonIgnore
    @JoinColumn(name = "cabinet_id", referencedColumnName = "id_cabinet")
    private Cabinet cabinet;

    @Enumerated(EnumType.STRING)
    private CabinetStatuMedcinEnum status;


    public CabinetMedecinsSpace() {
        super();
    }

    public CabinetMedecinsSpace(Medecin medecin, Cabinet cabinet, CabinetStatuMedcinEnum status) {
        super();
        this.id = new CabinetMedecinsIDs(medecin.getId(),cabinet.getId_cabinet());
        this.medecin = medecin;
        this.cabinet = cabinet;
        this.status = status;
    }
    
}
