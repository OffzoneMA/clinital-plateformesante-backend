package com.clinitalPlatform.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExpertisesMedecinDto implements Serializable {

    private Long id;
    private String nom_exp;

}
