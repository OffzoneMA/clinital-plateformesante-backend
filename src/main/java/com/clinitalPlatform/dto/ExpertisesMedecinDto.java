package com.clinitalPlatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExpertisesMedecinDto implements Serializable {

    private final Long id;
    private final String nom_exp;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final List<MedecinDTO> medecins;
}
