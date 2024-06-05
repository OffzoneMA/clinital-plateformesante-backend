package com.clinitalPlatform.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class CabinetMedecinsIDs implements Serializable {

    @Column(name = "medecin_id")
    private Long medecin_id;

    @Column(name = "cabinet_id")
    private Long cabinet_id;

    public CabinetMedecinsIDs() {
        super();
    }

    public CabinetMedecinsIDs(Long medecin_id, Long cabinet_id) {
        super();
        this.medecin_id = medecin_id;
        this.cabinet_id = cabinet_id;
    }
    
}
