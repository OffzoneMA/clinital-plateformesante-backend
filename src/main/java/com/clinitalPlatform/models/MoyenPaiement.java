package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.TypeMoyenPaiementEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "moyenspaiement")
@Data
public class MoyenPaiement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_mp;

	@Enumerated(EnumType.STRING)
	private TypeMoyenPaiementEnum type;

	@ManyToMany(mappedBy = "moyenPaiement")
	@JsonIgnore
	private List<Medecin> medecins;

	// Constructeur par défaut
	public MoyenPaiement() {
		super();
	}

	// Constructeur avec l'énumération comme paramètre
	public MoyenPaiement(TypeMoyenPaiementEnum type) {
		super();
		this.type = type;
	}

	public String getDescription() {
		return this.type.getDescription();
	}
}
