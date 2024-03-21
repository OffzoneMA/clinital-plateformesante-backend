package com.clinitalPlatform.dto;

import java.util.Date;

import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.enums.PatientTypeEnum;

import lombok.Data;

@Data
public class PatientDTO {

	private Long id;
	
	private String nom_pat;
	
	private String prenom_pat;
	
	private Date dateNaissance;
	
	private String adresse_pat;
	
	private String codePost_pat;
	
	private String matricule_pat;
	private CiviliteEnum civilite_pat;
	private VilleDTO ville;
	private DossierMedicalDTO dossierMedical;
	private UserDTO user;
	private String placeOfBirth;
	private String mutuelNumber;

	private String patientEmail;
	private String patientTelephone;
	private PatientTypeEnum patient_type;

}
