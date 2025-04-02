package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.CabinetDocStateEnum;
import com.clinitalPlatform.enums.CabinetDocuemtsEnum;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentsCabinetDTO {
    private long id;
    private CabinetDocuemtsEnum type_doc;
    private LocalDate date_ajout_doc;
    private String fichier_doc;
    private String nom_fichier;
    private Long id_cabinet;
    private Long id_medecin;
    private CabinetDocStateEnum validationState;
}