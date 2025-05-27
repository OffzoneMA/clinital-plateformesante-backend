package com.clinitalPlatform.payload.request;

import lombok.Data;

@Data
public class CompteRenduRdvRequest {
    private String contenu;
    private Long rdvId;
    private Long patientId;
}
