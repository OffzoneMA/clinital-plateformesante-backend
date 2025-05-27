package com.clinitalPlatform.payload.request;

import javax.validation.constraints.NotNull;
import lombok.Data;


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

	private Double longitude;

	private Double latitude;

    private CabinetMedecinsSpaceRequest cabinetmedecin;

	@NotNull
	private long id_medecin;

	// Payment information fields
	@NotNull
	private String intituleCompte;

	@NotNull
	private String rib;

	@NotNull
	private String codeSwift;


}
