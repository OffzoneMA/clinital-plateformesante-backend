package com.clinitalPlatform.dto;

public class PatientCountsDTO {
    private Long femmeCount;
    private Long hommeCount;
    private Long enfantCount;

    public PatientCountsDTO(Long femmeCount, Long hommeCount, Long enfantCount) {
        this.femmeCount = femmeCount;
        this.hommeCount = hommeCount;
        this.enfantCount = enfantCount;
    }

    // Getters and setters
    public Long getFemmeCount() {
        return femmeCount;
    }

    public void setFemmeCount(Long femmeCount) {
        this.femmeCount = femmeCount;
    }

    public Long getHommeCount() {
        return hommeCount;
    }

    public void setHommeCount(Long hommeCount) {
        this.hommeCount = hommeCount;
    }

    public Long getEnfantCount() {
        return enfantCount;
    }

    public void setEnfantCount(Long enfantCount) {
        this.enfantCount = enfantCount;
    }
}
