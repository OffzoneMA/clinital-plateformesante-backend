package com.clinitalPlatform.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class SecritaireRequest {

	@NotNull
	private String nom;
	@NotNull
	private String prenom;
	@NotNull
	private Date dateNaissance;
	@NotNull
	private String adresse;
	@NotNull
	private Long cabinetid;
	
}
