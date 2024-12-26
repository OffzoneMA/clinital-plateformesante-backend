package com.clinitalPlatform.dto;

import com.clinitalPlatform.enums.TypeMoyenPaiementEnum;
import lombok.Data;

@Data
public class MoyenPaiementDTO {
	private Long id_mp;
	private String type;
	private String description; // Ajout du champ description
	private VirementBancaireDTO virementBancaire;

	// Méthode pour récupérer la description basée sur le type
	public void setType(TypeMoyenPaiementEnum type) {
		this.type = type.name();
		this.description = type.getDescription(); // Récupération de la description
	}

}
