package com.clinitalPlatform.payload.response;

import lombok.Data;

import java.util.Date;

@Data
public class CabinetResponse {
	
	private Long id_cabinet;
	private String nom;
	private String adresse;
	private String code_post;
	private Date horaires;
	private String phoneNumber;
	

}
