package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.ConsultationPeriodEnum;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoraireCabinetRequest {
    private DayOfWeek day;

    private List<TimeSlot> timeSlots;

    private boolean ferme;

    private Long cabinetId;

    @Data
    public static class TimeSlot {
        private Long id;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
