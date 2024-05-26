package com.clinitalPlatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
public class ExperienceMedecinDTO {

    private String nom_experience;

    private Date date_debut;

    private Date date_fin;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MedecinDTO medecinDTO;


}
