package com.clinitalPlatform.dto;

import lombok.Data;

@Data
public class VirementBancaireDTO {
    private Long id_vb;
    private String rib;
    private String codeSwift;
    private String bankName;
    private Long medecinId;
}