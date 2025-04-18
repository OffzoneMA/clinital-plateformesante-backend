package com.clinitalPlatform.dto;

import lombok.Data;

@Data
public class SpecialiteDTO {

	private Long id_spec;
	private String libelle;
	private boolean valid = true;

}
