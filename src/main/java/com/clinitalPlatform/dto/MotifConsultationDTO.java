package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.MotifConsultationEnum;

import lombok.Data;

@Data
public class MotifConsultationDTO {

    private Long id_motif;
	private MotifConsultationEnum motif;

}
