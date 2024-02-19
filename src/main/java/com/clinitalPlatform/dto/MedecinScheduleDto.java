package com.clinitalPlatform.dto;

import lombok.Data;

import com.clinitalPlatform.enums.ConsultationPeriodEnum;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Data
public class MedecinScheduleDto implements Serializable {

    private  DayOfWeek day;

    private  LocalDateTime availabilityStart;

    private  LocalDateTime availabilityEnd;

    private ModeConsultationDTO Mode;

    private  ConsultationPeriodEnum periode;

    private MedecinDTO medecin;

    private Long cabinet_id;

    public MedecinScheduleDto(){
        super();
    }
}
