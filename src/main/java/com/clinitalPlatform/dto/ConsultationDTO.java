package com.clinitalPlatform.dto;

import java.time.LocalDate;

import com.clinitalPlatform.models.Rendezvous;

import lombok.Data;

@Data
public class ConsultationDTO {
	private Long id_consultation;
	private String Details;
	private LocalDate date;
	private MedecinDTO medecin;
	private PatientDTO patient;
	private Rendezvous rendezvous;


}
