package com.clinitalPlatform.dto;

import lombok.Data;

@Data
public class FactureDTO {

	private Long id_facture;
	private String num_facture;
	private float montant;
	private String libelle;
	private boolean etat;
	private ConsultationDTO consultation;
	private MoyenPaiementDTO moyenPaiement;

}
