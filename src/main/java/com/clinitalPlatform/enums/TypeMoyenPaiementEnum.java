package com.clinitalPlatform.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeMoyenPaiementEnum {
    Cheque("Chèque"),
    Cash("Espèces"),
    Credit("Carte Bancaire"),
    Virement("Virement Bancaire");

    private final String description;

    TypeMoyenPaiementEnum(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }
}
