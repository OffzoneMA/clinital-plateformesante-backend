package com.clinitalPlatform.payload.response;

import com.clinitalPlatform.enums.ConsultationPeriodEnum;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Data
public class MedecinScheduleResponse {

    private Long id;
    private DayOfWeek day;
    private LocalDateTime availabilityStart;

    private LocalDateTime availabilityEnd;

    private ConsultationPeriodEnum period;

    private long idMedecin;

}
