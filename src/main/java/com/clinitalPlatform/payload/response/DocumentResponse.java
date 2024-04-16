package com.clinitalPlatform.payload.response;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentResponse {

	private Long id_doc;
	private Long numero_doc;
	private String titre_doc;
	private Date date_ajout_doc;
	private String auteur;
	private String fichier_doc;
	private TypeDocumentResponse typeDoc;
	private Boolean archived;
	private DossierMedicalResponse dossier;

}
