package com.clinitalPlatform.dto;

import java.util.List;

import com.clinitalPlatform.enums.PatientTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DossierMedicalDTO {

	private Long id_dossier;
	private String numDossier;
	private boolean traitement;
	private PatientTypeEnum dossierType;
	private String accesscode;
	private boolean fumeur;
	private boolean alchole;
	@JsonIgnore
	private List<DocumentDTO> documents;

}
