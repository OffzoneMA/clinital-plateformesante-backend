package com.clinitalPlatform.payload.request;

import com.clinital.enums.CabinetStatuMedcinEnum;
import lombok.Data;

@Data
public class CabinetMedecinsSpaceRequest {

    private long medecin_id;
    private long cabinet_id;
    private CabinetStatuMedcinEnum status;

    
}
