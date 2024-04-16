package com.clinitalPlatform.services;

import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(value = Include.NON_NULL)
public class RendezvousRequest {

	
	@JsonProperty("day")
	private String day;
	private LocalDateTime start;
	private LocalDateTime end;
	private LocalDateTime canceledat;
	private RdvStatutEnum statut;
    private Long modeconsultation;
	private MotifConsultationEnum motif;
	private Long medecinid;
	private Long patientid;
	

//private Long typeConsultationId;
//	private Long medecinScheduleId;

}
