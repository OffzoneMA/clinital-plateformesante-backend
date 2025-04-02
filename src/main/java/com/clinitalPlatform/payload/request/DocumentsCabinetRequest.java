package com.clinitalPlatform.payload.request;

import java.time.LocalDate;

import com.clinitalPlatform.enums.CabinetDocStateEnum;
import com.clinitalPlatform.enums.CabinetDocuemtsEnum;

import lombok.Data;

@Data
public class DocumentsCabinetRequest {

    private Long Id_doccab;
    private CabinetDocuemtsEnum type;
    private LocalDate addDate;
	private String fichier_doc;
    private long id_cabinet;
    private CabinetDocStateEnum docstate;
    private  long id_medecin;
    private String nom_fichier;

}
