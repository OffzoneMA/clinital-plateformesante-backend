package com.clinitalPlatform.dto;
import com.clinitalPlatform.models.Assistant;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Secretaire;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class EquipeDTO {
    private List<Secretaire> secretaires;
    private List<Assistant> assistants;

    private List<MedecinDTO> medecinDTOS;
}
