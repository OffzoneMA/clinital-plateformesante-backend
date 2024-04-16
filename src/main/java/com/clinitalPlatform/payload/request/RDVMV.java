package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Data
@JsonInclude(value = Include.NON_NULL)
public class RDVMV {


    private LocalDateTime end;
    private LocalDateTime start;
    private Long medecinid;



//private Long typeConsultationId;
//	private Long medecinScheduleId;

}
