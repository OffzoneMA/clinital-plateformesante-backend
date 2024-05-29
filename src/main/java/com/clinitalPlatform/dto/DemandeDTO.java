package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.DemandeStateEnum;

import lombok.Data;

@Data
public class DemandeDTO {
	
	private Long id;
	
	private String nom_med;
	
	private String prenom_med;
	
	private String matricule;
	
	private String mail;
	
	private String specialite;
	
	private String inpe;
	
	private String nom_cab;

	private String adresse;
	
	private String code_postal;

	private DemandeStateEnum validation;

}
