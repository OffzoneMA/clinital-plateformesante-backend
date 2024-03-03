package com.clinitalPlatform.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetDossierRequest {
    @NotNull
    private Long iddoss;
    private String codeaccess;
    
}
