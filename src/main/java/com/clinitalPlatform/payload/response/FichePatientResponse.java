package com.clinitalPlatform.payload.response;

import com.clinitalPlatform.dto.VilleDTO;
import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.models.Antecedents;
import com.clinitalPlatform.models.Document;
import com.clinitalPlatform.models.Rendezvous;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class FichePatientResponse {
    
    private Long id;

	private String nom_pat;
	private String prenom_pat;
	private Date dateNaissance;
	private String adresse_pat;
	private String codePost_pat;
	private String matricule_pat;
	private CiviliteEnum civilite_pat;
	private VilleDTO ville;
	private String placeOfBirth;
	private String mutuelNumber;
	private String patientEmail;
	private String patientTelephone;
    private List<Rendezvous> allrdv= new ArrayList<Rendezvous>();
    private List<Document> alldoc = new ArrayList<Document>();
	private List<Antecedents> Allantecedents = new ArrayList<Antecedents>();
}
