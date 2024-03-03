package com.clinitalPlatform.payload.request;

import lombok.Data;

@Data
public class TypeConsultationRequest {

	private String title;
	private double tarif;
	private Long medecinId;

}
