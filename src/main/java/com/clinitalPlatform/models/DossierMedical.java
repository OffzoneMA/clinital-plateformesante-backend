package com.clinitalPlatform.models;

import java.time.LocalDateTime;
import java.util.List;

import com.clinitalPlatform.enums.PatientTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "dossiers")
@Data
public class DossierMedical {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_dossier;
	private String numDossier;
	private boolean traitement;
	
	@Enumerated(value = EnumType.STRING)
	@Column(name = "dossier_type")
	private PatientTypeEnum dossierType;
	
	private String accesscode;
	private boolean fumeur;
	private boolean alchole;
	
	@OneToMany(mappedBy = "dossier")
	@JsonIgnore
	private List<Document> documents;

	@OneToMany(mappedBy = "dossier")
	@JsonIgnore
	private List<Ordonnance> Ordonnance;

	@OneToMany(mappedBy = "dossier")
	@JsonIgnore
	private List<Consultation> consulations;

	@OneToMany(mappedBy = "dossier")
	//@JsonIgnore
	private List<Antecedents> antecedents;

	@OneToMany(mappedBy = "dossier")
	@JsonIgnore
	private List<Allergies>  allergies;
	
	@ManyToMany(fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      },
      mappedBy = "Meddossiers")
  	@JsonIgnore
  	private List<Medecin> medecins;

	//Metadonnees
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
