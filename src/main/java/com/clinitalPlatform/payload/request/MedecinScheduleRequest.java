package com.clinitalPlatform.payload.request;

import com.clinital.enums.ConsultationPeriodEnum;
import com.clinital.models.ModeConsultation;
import com.clinital.models.MotifConsultation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedecinScheduleRequest {

	private Long id;
	private String day;
	private LocalDateTime availabilityStart;
	private LocalDateTime availabilityEnd;
	private ConsultationPeriodEnum period;
	private List<ModeConsultation> modeconsultation;
	private List<MotifConsultation> motifconsultation;
	private Long medecin_id;
	private Long cabinet_id;
	private Boolean isnewpatient;
}
