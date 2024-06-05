package com.clinitalPlatform.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class DocumentDTO {
	private Long id_doc;
	
	private Long numero_doc;
	
	private String titre_doc;
	
	private Date date_ajout_doc;
	
	private String auteur;
	private String fichier_doc;
	private PatientDTO patient;
	private DossierMedicalDTO dossier;
	private List<MedecinDTO> medecins;
	private Boolean archived;

}
