package com.clinitalPlatform.payload.response;

import com.clinital.enums.MotifConsultationEnum;
import com.clinital.enums.RdvStatutEnum;
import com.clinital.models.ModeConsultation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RendezvousResponse {
	private Long id;
	@JsonProperty("day")
	@NotNull
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


}
