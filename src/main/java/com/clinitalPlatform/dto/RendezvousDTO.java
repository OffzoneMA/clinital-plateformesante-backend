package com.clinitalPlatform.dto;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import com.clinitalPlatform.enums.RdvStatutEnum;

import lombok.Data;

@Data
public class RendezvousDTO {

	private Long id;
	private DayOfWeek Day;
	private LocalDateTime start;
	private LocalDateTime end;
	private LocalDateTime canceledat;
	private RdvStatutEnum statut;
	private Long modeconsultation;
	private Long medecinid;
	private long patientid;
	private Boolean isnewpatient;
	private String commantaire;
	private Long motif;
	private String LinkVideoCall;
	private Long cabinet;


	public String getLinkVideocall() {
		return LinkVideoCall;
	}
}
