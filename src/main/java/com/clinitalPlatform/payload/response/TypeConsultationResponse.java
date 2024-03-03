package com.clinitalPlatform.payload.response;

import lombok.Data;

@Data
public class TypeConsultationResponse {

	private Long consultationId;
	private String title;
	private double tarif;
	private Long medecinId;

}
