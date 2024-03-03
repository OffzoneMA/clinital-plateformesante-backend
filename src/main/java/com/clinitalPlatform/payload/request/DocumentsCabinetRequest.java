package com.clinitalPlatform.payload.request;

import com.clinital.enums.CabinetDocuemtsEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentsCabinetRequest {

    private Long Id_doccab;
    private CabinetDocuemtsEnum type;
    private LocalDate addDate;
	private String fichier_doc;
    private long id_cabinet;
    private CabinetDocuemtsEnum docstate;
    
}
