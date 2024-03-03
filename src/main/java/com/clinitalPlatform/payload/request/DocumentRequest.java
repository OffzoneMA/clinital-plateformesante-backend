package com.clinitalPlatform.payload.request;

import lombok.Data;

@Data
public class DocumentRequest {

	private String titre_doc;
	private String auteur;
	private Long patientId;
	private Long rdvId;
	private Long typeDocId;

}
