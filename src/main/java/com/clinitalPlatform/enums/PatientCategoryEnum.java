package com.clinitalPlatform.enums;

public enum PatientCategoryEnum {
    NOUVEAU_PATIENT("Nouveau Patient"),
    PATIENT_SUIVI("Patient Suivi");

    private final String displayName;

    PatientCategoryEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}