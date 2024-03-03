package com.clinitalPlatform.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CabinetRequest {
    
    private Long id_cabinet;
	@NotNull
	private String nom;
	@NotNull
	private String adresse;
	@NotNull
	private String code_post;
	@NotNull
	private Long id_ville;
	@NotNull
	private String phoneNumber;
	//private List<SecretaireDTO> secretaires;
	//private List<MedecinDTO> medecins;
    private CabinetMedecinsSpaceRequest cabinetmedecin;

	@NotNull
	private long id_medecin;


}
