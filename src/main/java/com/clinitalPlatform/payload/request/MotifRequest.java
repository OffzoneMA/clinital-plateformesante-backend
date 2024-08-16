package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.MotifConsultationEnum;
import lombok.Data;

import java.util.List;

@Data
public class MotifRequest {

    private List<Long> medecinIds;
    private List<String> libellesMotifs;

}


