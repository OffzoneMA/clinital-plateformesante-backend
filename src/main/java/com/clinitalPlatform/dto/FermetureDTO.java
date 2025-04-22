package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.MotifFermetureEnum;
import com.clinitalPlatform.models.FermetureExceptionnelle;
import com.clinitalPlatform.models.Medecin;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FermetureDTO {

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Long medecinId;
    private List<Long> motifIds;

    public FermetureExceptionnelle toEntity() {
        FermetureExceptionnelle fermeture = new FermetureExceptionnelle();
        fermeture.setDateDebut(this.dateDebut);
        fermeture.setDateFin(this.dateFin);
        Medecin medecin = new Medecin();
        medecin.setId(this.medecinId);
        fermeture.setMedecin(medecin);
        return fermeture;
    }
}

