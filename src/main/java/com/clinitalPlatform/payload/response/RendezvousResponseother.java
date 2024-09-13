package com.clinitalPlatform.payload.response;

import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.models.ModeConsultation;
import com.clinitalPlatform.models.MotifConsultation;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

        import java.time.LocalDateTime;

@Data
public class RendezvousResponseother  {
    private Long id;

    @JsonProperty("day")
    @NotNull
    private String day;

    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime canceledat;

    private RdvStatutEnum statut;

    // Champs pour stocker les IDs
    private Long modeconsultationId;
    private Long motifId;


    private Long medecinid;
    private Long patientid;
    private String LinkVideoCall;

    // Ajout des champs manquants
    private Boolean isnewpatient;
    private String commantaire;
    private Long cabinet; // Assurez-vous que le type de données correspond à celui utilisé dans Rendezvous
}