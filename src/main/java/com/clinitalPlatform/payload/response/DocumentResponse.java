package com.clinitalPlatform.payload.response;

import com.clinitalPlatform.models.Patient;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

import java.util.Date;

@Data
public class DocumentResponse {

	private Long id_doc;
	private Long numero_doc;
	private String titre_doc;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
	private Date date_ajout_doc;

	private String auteur;
	private String fichier_doc;
	private Patient patient;
	private TypeDocumentResponse typeDoc;
	private Boolean archived;
	private DossierMedicalResponse dossier;

}
