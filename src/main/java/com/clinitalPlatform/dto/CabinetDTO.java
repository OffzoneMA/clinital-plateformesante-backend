package com.clinitalPlatform.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class CabinetDTO {
	private Long id_cabinet;
	
	private String nom;
	
	private String adresse;
	
	private String code_post;
	
	private Date horaires;
	
	private String phoneNumber;
	
	private List<MedecinDTO> medecins;

}
