package com.clinitalPlatform.payload.request;

import com.clinital.enums.AntecedentTypeEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AntecedentsRequest {
    
    private Long id_anticedent;

    private AntecedentTypeEnum type;

    private String descreption;

    private LocalDate date;
	
    private Long dossier;
}
