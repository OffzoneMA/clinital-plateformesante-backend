package com.clinitalPlatform.payload.request;

import java.time.LocalDate;

import lombok.Data;
@Data
public class OrdonnanceRequest {

    private Long id_ordonnance;
	private String Details;
	private LocalDate date;
	private Long medecin;
	private Long dossier;
	private Long rendezvous;
    
}
