package com.clinitalPlatform.payload.response;

import com.clinital.dto.MoyenPaiementDTO;
import com.clinital.dto.SpecialiteDTO;
import com.clinital.dto.UserDTO;
import com.clinital.dto.VilleDTO;
import com.clinital.enums.CiviliteEnum;
import lombok.Data;

import java.util.List;

@Data
public class MedecinResponse {

	private Long id;

	private String matricule_med;
	private String nom_med;
	private String prenom_med;
	private String photo_med;
	private String diplome_med;
	private String experience_med;
	private String description_med;
	private CiviliteEnum civilite_med;
	private VilleDTO ville;
	private SpecialiteDTO specialite;
	private CabinetResponse cabinet;
	private List<MoyenPaiementDTO> moyenPaiement;
	private String telephone;
	private UserDTO user;

}
