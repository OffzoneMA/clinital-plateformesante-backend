package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.ConsultationPeriodEnum;
import com.clinitalPlatform.models.ModeConsultation;
import com.clinitalPlatform.models.MotifConsultation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedecinScheduleConfigRequest {
    private Long id;
    private String day;
    private LocalDateTime availabilityStart;
    private LocalDateTime availabilityEnd;
    private ConsultationPeriodEnum period;
    private List<ModeConsultation> modeconsultation;
    private List<MotifConsultation> motifconsultation;
    private Long medecinId;
    private Long cabinetId;
    private Boolean allowNewPatients;
    private Boolean allowFollowUpPatients;
}
