package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.ConsultationPeriodEnum;
import com.clinitalPlatform.models.ModeConsultation;
import com.clinitalPlatform.models.MotifConsultation;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class MedecinMultiScheduleRequest {
    private Long id;

    // Map pour stocker les créneaux par jour
    private Map<DayOfWeek, List<TimeSlot>> schedules;

    // Categories
    private Boolean allowNewPatients;
    private Boolean allowFollowUpPatients;

    // Existing fields
    private List<ModeConsultation> modesConsultation;
    private List<MotifConsultation> motifsConsultation;
    private Long medecinId;
    private Long cabinetId;

    // Inner class for time slots
    @Data
    public static class TimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private ConsultationPeriodEnum period;
    }
}
