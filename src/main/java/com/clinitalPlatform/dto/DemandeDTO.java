package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.enums.DemandeStateEnum;

import lombok.Data;

@Data
public class DemandeDTO {
	
	private Long id;
	
	private String nom_med;
	
	private String prenom_med;
	
	private String phonenumber;
	
	private String mail;
	
	private String specialite;
	
	private String inpe;
	
	private String ville;
	
	private DemandeStateEnum validation;
	
	private CiviliteEnum civilite_med;

}
