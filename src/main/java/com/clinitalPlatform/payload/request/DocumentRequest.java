package com.clinitalPlatform.payload.request;

import lombok.Data;
import com.clinitalPlatform.enums.AuteurDocumentType;

@Data
public class DocumentRequest {

	private String titre_doc;
	private String auteur;
	private Long patientId;
	private Long rdvId;
	private Long typeDocId;
	private AuteurDocumentType auteurDocumentType = AuteurDocumentType.PATIENT;
	private Long medecinId;
	private String categorie = "patient-docs";

}
