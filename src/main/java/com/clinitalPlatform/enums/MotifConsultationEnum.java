package com.clinitalPlatform.enums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public enum MotifConsultationEnum {
    CONSULTATION,CONSULTATIONSUIVIE,URGENCE;

    MotifConsultationEnum() {
    }
}
