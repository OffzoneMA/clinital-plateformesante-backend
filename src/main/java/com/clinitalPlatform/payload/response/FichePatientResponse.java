package com.clinitalPlatform.payload.response;

import com.clinital.dto.VilleDTO;
import com.clinital.enums.CiviliteEnum;
import com.clinital.models.Antecedents;
import com.clinital.models.Document;
import com.clinital.models.Rendezvous;
import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
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

	@Data
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	public static class RendezvousRequest {


		@JsonProperty("day")
		private String day;
		private LocalDateTime start;
		private LocalDateTime end;
		private LocalDateTime canceledat;
		private RdvStatutEnum statut;
		private Long modeconsultation;
		private MotifConsultationEnum motif;
		private Long medecinid;
		private Long patientid;


	//private Long typeConsultationId;
	//	private Long medecinScheduleId;

	}
}
