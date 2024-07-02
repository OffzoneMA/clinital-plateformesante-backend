package com.clinitalPlatform.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
public class ExpertisesMedecinDto implements Serializable {

    private Long id;
    private String nom_exp;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<MedecinDTO> medecins;
}
