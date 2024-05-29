package com.clinitalPlatform.models;


import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "factures")
@Data
public class Facture {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_facture;
	private String num_facture;
	private float montant;
	private String libelle;
	private boolean etat;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="Consultation")
	private Consultation consultation;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "moyen", nullable = false, referencedColumnName = "id_mp", insertable = true, updatable = true)
	private MoyenPaiement moyenPaiement;

	public Facture() {
		super();
	}

	public Facture(String num_facture, float montant, String libelle, boolean etat, Consultation consultation,
			MoyenPaiement moyenPaiement) {
		super();
		this.num_facture = num_facture;
		this.montant = montant;
		this.libelle = libelle;
		this.etat = etat;
		this.consultation = consultation;
		this.moyenPaiement = moyenPaiement;
	}

	
	
	
	

}
