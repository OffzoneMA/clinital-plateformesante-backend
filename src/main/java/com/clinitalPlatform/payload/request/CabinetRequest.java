package com.clinitalPlatform.payload.request;

import lombok.Data;


@Data
public class CabinetRequest {
    
    private Long id_cabinet;

	private String nom;

	private String adresse;

	private String code_post;
	private Long id_ville;
	private String phoneNumber;
	//private List<SecretaireDTO> secretaires;
	//private List<MedecinDTO> medecins;
    //private CabinetMedecinsSpaceRequest cabinetmedecin;

	//private long id_medecin;


}
