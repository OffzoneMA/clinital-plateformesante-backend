package com.clinitalPlatform.dto;

import lombok.Data;

@Data
public class VilleDTO {
	private Long id_ville;
	private String nom_ville;
	private PaysDTO pays;

}
