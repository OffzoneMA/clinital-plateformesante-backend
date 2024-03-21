package com.clinitalPlatform.dto;

import java.util.Date;

import lombok.Data;

@Data
public class SecretaireDTO{
	private Long id;
	
	private String nom;
	
	private String prenom;
	
	private Date dateNaissance;
	
	private String adresse;
	
	private CabinetDTO cabinet;
	
	private UserDTO user;
	
	
}
