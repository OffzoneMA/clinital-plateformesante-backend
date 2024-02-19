package com.clinitalPlatform.models;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "consultations")
@Data
public class Consultation {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_consultation;
	private String Details;
	private LocalDate date;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_medecin", nullable = false, referencedColumnName = "id", insertable = true, updatable = true)
	private Medecin medecin;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_patient", nullable = false, referencedColumnName = "id", insertable = true, updatable = true)
	private Patient patient;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_dossier", nullable = false, referencedColumnName = "id_dossier", insertable = true, updatable = true)
	private DossierMedical dossier;

	@OneToOne
	private Rendezvous rendezvous;
	
	public Consultation() {
		super();
	}
	
	public Consultation(String details, LocalDate date, Medecin medecin, Patient patient, Rendezvous rdv,DossierMedical dossier) {
		super();
		this.Details = details;
		this.date = date;
		this.medecin = medecin;
		this.patient = patient;
		this.rendezvous=rdv;
		this.dossier=dossier;
	}

	
	
}
