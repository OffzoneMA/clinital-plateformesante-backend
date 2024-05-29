package com.clinitalPlatform.dto;

import com.clinitalPlatform.models.CabinetMedecinsIDs;

import lombok.Data;

@Data
public class CabinetMedecinDTO {

    private CabinetMedecinsIDs id;
    private long medecin_id;
    private long cabinet_id;
    private CabinetMedecinsIDs status;
    
    public CabinetMedecinDTO() {
        super();
    }
    
    public CabinetMedecinDTO(long medecin, long follower, CabinetMedecinsIDs status) {
        super();
        this.medecin_id = medecin;
        this.cabinet_id = follower;
        this.status = status;
        this.id=new CabinetMedecinsIDs(medecin, follower);
    }
    
}
