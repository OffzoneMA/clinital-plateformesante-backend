package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.CiviliteEnum;
import lombok.Data;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Data
public class MedecinRequest {

    private Long id;
	private String matricule_med;
	private String inpe;
	private String nom_med;
	private String prenom_med;
	private String photo_med;
	private String photo_couverture_med;
	private Long diplome_med;
	private String description_med;
	private String contact_urgence_med;
	@Enumerated(value = EnumType.STRING)
	private CiviliteEnum civilite_med;
	private Long ville;
	private Long specialite;
	private Long user;
	private Long cabinet;
	
	

	

 
    
}
