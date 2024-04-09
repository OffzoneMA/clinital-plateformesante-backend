package com.clinitalPlatform.models;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.enums.PatientTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "patients")
@Data
public class Patient {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nom_pat;

	private String prenom_pat;

	@Temporal(TemporalType.TIMESTAMP)
	private Date dateNaissance;

	private String adresse_pat;

	private String codePost_pat;

	private String matricule_pat;

	@Enumerated(value = EnumType.STRING)
	private CiviliteEnum civilite_pat;

	@Column(name = "patient_email")
	private String patientEmail;

	@Column(name = "patient_telephone")
	private String patientTelephone;

	@Column(name = "place_of_birth")
	private String placeOfBirth;

	@Column(name = "mutuel_number")
	private String mutuelNumber;

	@Enumerated(value = EnumType.STRING)
	private PatientTypeEnum patient_type;

	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "id_ville", nullable = true, referencedColumnName = "id_ville", insertable = true, updatable = true)
	@JsonIgnore
	private Ville ville;

	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "id_dossier", nullable = true, referencedColumnName = "id_dossier", insertable = true, updatable = true)
	@JsonIgnore
	private DossierMedical dossierMedical;

	@OneToMany(mappedBy = "patient")
	@JsonIgnore
	private List<Consultation> Consultations;

	@OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	@JsonIgnore
	private User user;

	public Patient() {
		super();
	}

	public Patient(@NotNull String nom_pat, @NotNull String prenom_pat, @NotNull Date dateNaissance,
			@NotNull String adresse_pat, @NotNull String codePost_pat, @NotNull String matricule_pat,
			CiviliteEnum civilite_pat, Ville ville, DossierMedical dossierMedical, List<Rendezvous> lesrdvs,
			PatientTypeEnum patient_type,User user ) {
		super();
		this.nom_pat = nom_pat;
		this.prenom_pat = prenom_pat;
		this.dateNaissance = dateNaissance;
		this.adresse_pat = adresse_pat;
		this.codePost_pat = codePost_pat;
		this.matricule_pat = matricule_pat;
		this.civilite_pat = civilite_pat;
		this.ville = ville;
		this.dossierMedical = dossierMedical;
		this.patient_type = patient_type;
		this.user = user;
	
	}

}
