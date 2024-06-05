package com.clinitalPlatform.dto;

import java.time.LocalDate;

import com.clinitalPlatform.enums.AntecedentTypeEnum;

import lombok.Data;

@Data
public class AntecedentsDTO {
    
    private Long id_anticedent;

    private AntecedentTypeEnum type;

    private String descreption;

    private LocalDate date;
	
    private DossierMedicalDTO dossier;
}
