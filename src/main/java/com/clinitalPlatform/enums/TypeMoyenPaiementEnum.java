package com.clinitalPlatform.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeMoyenPaiementEnum {
    Cheque("Chèque"),
    Cash("Espèces"),
    Credit("Carte Bancaire"),
    Virement("Virement Bancaire"),
    GooglePay("Google Pay"),
    ApplePay("Apple Pay"),
    Paypal("Paypal");

    private final String description;

    TypeMoyenPaiementEnum(String description) {
        this.description = description;
    }

    public static TypeMoyenPaiementEnum getEnumByString(String type) {
        for (TypeMoyenPaiementEnum moyen : TypeMoyenPaiementEnum.values()) {
            if (moyen.name().equalsIgnoreCase(type)) {
                return moyen;
            }
        }
        throw new IllegalArgumentException("Type de moyen de paiement non valide: " + type);
    }

    @JsonValue
    public String getDescription() {
        return description;
    }
}
