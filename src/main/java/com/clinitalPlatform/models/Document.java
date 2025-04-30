package com.clinitalPlatform.models;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "documents")
@Data
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_doc;
	@Column(name = "numero_doc", nullable = true)
	private Long numero_doc;
	private String titre_doc;
	private Date date_ajout_doc;
	private String auteur;
	private String fichier_doc;

	@Column(name = "date_modif_doc", nullable = true)
	private Date date_modif_doc;

	@ManyToOne
	@JoinColumn(name = "patient_id", nullable = false, referencedColumnName = "id", insertable = true, updatable = true)
	@JsonIgnore
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "id_dossier", nullable = false, referencedColumnName = "id_dossier", insertable = true, updatable = true)
	private DossierMedical dossier;

	@Column(name = "archived", nullable = true, columnDefinition = "tinyint(1) default 0")
	private Boolean archived;

	@ManyToOne
	@JoinColumn(name = "type_doc", nullable = false, referencedColumnName = "id_typedoc", insertable = true, updatable = true)
	private TypeDocument typeDoc;

	@ManyToOne
	@JoinColumn(name = "rdv_id", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
	@JsonIgnore
	private Rendezvous rendezvous;


	@ManyToMany(fetch = FetchType.LAZY,
      cascade = {
          CascadeType.ALL,
      },
      mappedBy = "Meddoc")
  	@JsonIgnore
  	private List<Medecin> medecins;

	public Document() {
		super();
	}

	public Document(@NotBlank Long numero_doc, @NotBlank String titre_doc, @NotBlank Date date_ajout_doc,
			@NotBlank String auteur, @NotBlank String fichier_doc, Patient patient, DossierMedical dossier,
			List<Medecin> medecins) {
		super();
		this.numero_doc = numero_doc;
		this.titre_doc = titre_doc;//
		this.date_ajout_doc = date_ajout_doc;
		this.auteur = auteur;
		this.fichier_doc = fichier_doc;
		this.patient = patient;
		this.dossier = dossier;
	}

	@PrePersist
	protected void onCreate() {
		this.date_ajout_doc = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		this.date_modif_doc = new Date();
	}

}
