package com.clinitalPlatform.payload.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConsultationRequest {

    private Long id_consultation;
	private String Details;
	private LocalDate date;
	private Long medecin;
	private Long patient;
	private Long Rendezvous;
    
}

