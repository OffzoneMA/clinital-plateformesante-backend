package com.clinitalPlatform.dto;

import java.util.List;

import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.models.CabinetMedecinsSpace;
import com.clinitalPlatform.models.ExperienceMedecin;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;

@Data 
public class MedecinDTO {

	private Long id;

	private String matricule_med;

	private String inpe;//AJOUT
	
	private String nom_med;
	
	private String prenom_med;
	
	private String photo_med;
	private String photo_couverture_med;//AJOUT
	
	private List<ExpertisesMedecinDto> expertisesMedecin;
	
	private List<DiplomeMedecinDTO> diplome_med;
	
	private List<ExperienceMedecin> experience_med;

	private  List<LangueDTO> langues;
	private List<TarifDTO> tarifs;
	
	private String description_med;
	private String contact_urgence_med;
	private CiviliteEnum civilite_med;
	
	//@JsonProperty(access = Access.WRITE_ONLY)
	private VilleDTO ville;
	
	//@JsonProperty(access = Access.WRITE_ONLY)
	private SpecialiteDTO specialite;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	private List<RendezvousDTO> lesrdvs;
	
	//@JsonProperty(access = Access.WRITE_ONLY)
	//private CabinetDTO cabinet;
	private List<CabinetDTO> cabinet;
	private List<CabinetMedecinsSpace> cabinets;
	//@JsonProperty(access = Access.WRITE_ONLY)
	private List<MoyenPaiementDTO> moyenPaiement;

	private List<VirementBancaireDTO> virementBancaires;

	private UserDTO user;
}
