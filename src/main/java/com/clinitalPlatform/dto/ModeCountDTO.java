package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.ModeConsultationEnum;

public class ModeCountDTO {

    private ModeConsultationEnum mode;
    private Long count;

    // Constructeur, getters et setters
    public ModeCountDTO(ModeConsultationEnum mode, Long count) {
        this.mode = mode;
        this.count = count;
    }

    public ModeConsultationEnum getMode() {
        return mode;
    }

    public void setMode(ModeConsultationEnum mode) {
        this.mode = mode;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}

