package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.ModeConsultationEnum;

public class ModeCountDTO {

    private Long cabinetCount;
    private Long videoCount;
    private Long domicileCount;

    public ModeCountDTO(Long cabinetCount, Long videoCount, Long domicileCount) {
        this.cabinetCount = cabinetCount;
        this.videoCount = videoCount;
        this.domicileCount = domicileCount;
    }

    public Long getCabinetCount() {
        return cabinetCount;
    }
    public void setCabinetCount(Long cabinetCount) {
        this.cabinetCount = cabinetCount;
    }



    public Long getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Long videoCount) {
        this.videoCount = videoCount;
    }

    public Long getDomicileCount() {
        return domicileCount;
    }

    public void setDomicileCount(Long enfantCount) {
        this.domicileCount = enfantCount;
    }
}


