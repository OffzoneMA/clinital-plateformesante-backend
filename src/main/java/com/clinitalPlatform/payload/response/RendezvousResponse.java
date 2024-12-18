package com.clinitalPlatform.payload.response;


import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.models.ModeConsultation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RendezvousResponse {
	private Long id;
	@JsonProperty("day")   //@NotNull
	private String day;
	private LocalDateTime start;
	private LocalDateTime end;
	private LocalDateTime canceledat;
	private RdvStatutEnum statut;
    private ModeConsultation modeconsultation;
	private MotifConsultationEnum motif;
	private Long medecinid;
	private Long patientid;
	private String LinkVideoCall;
	private PatientResponse patient;


}
