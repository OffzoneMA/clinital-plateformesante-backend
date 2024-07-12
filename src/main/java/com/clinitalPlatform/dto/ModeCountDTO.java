package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.ModeConsultationEnum;

public class ModeCountDTO {

    private ModeConsultationEnum mode;
    private int year;
    private int month;
    private Long count;

    // Constructor, getters, and setters
    public ModeCountDTO(ModeConsultationEnum mode, int year, int month, Long count) {
        this.mode = mode;
        this.year = year;
        this.month = month;
        this.count = count;
    }

    public ModeConsultationEnum getMode() {
        return mode;
    }

    public void setMode(ModeConsultationEnum mode) {
        this.mode = mode;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}


