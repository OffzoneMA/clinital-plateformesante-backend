package com.clinitalPlatform.dto;

import java.time.LocalDate;

import lombok.Data;


@Data
public class OrdonnanceDTO {

    private Long id_ordon;
	private String details;
	private LocalDate date;
	private MedecinDTO medecin;
	private DossierMedicalDTO dossier;
    
}
