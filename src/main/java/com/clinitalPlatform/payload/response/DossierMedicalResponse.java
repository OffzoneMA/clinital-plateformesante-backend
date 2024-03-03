package com.clinitalPlatform.payload.response;

import com.clinital.enums.PatientTypeEnum;
import lombok.Data;

@Data
public class DossierMedicalResponse {

	private Long id_dossier;
	private String numDossier;
	private boolean traitement;
	private PatientTypeEnum dossierType;
	private String accesscode;
	private boolean fumeur;
	private boolean alchole;
}
