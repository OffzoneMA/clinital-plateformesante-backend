package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.enums.PatientTypeEnum;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class PatientRequest {
	private Long id;

	@NotNull
	private String nom_pat;
	@NotNull
	private String prenom_pat;
	@NotNull
	private Date dateNaissance;
	@NotNull
	private String adresse_pat;
	private CiviliteEnum civilite_pat;
	@NotNull
	private String codePost_pat;
	@NotNull
	private String matricule_pat;
	private Long villeId;
	private String placeOfBirth;
	private String mutuelNumber;
	@Size(max = 50)
	@Email
	private String patientEmail;
	private String patientTelephone;
	private PatientTypeEnum patient_type;

}
