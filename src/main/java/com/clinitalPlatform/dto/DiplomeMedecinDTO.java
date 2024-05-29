package com.clinitalPlatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class DiplomeMedecinDTO {

    
    private String nom_diplome;

    
    private Date date_debut;

    
    private Date date_fin;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MedecinDTO medecinDTO;
}
