package com.clinitalPlatform.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "factures")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

	//Metadonnees utiles
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
