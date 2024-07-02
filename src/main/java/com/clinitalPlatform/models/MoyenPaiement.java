package com.clinitalPlatform.models;

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
	private String type;

	@ManyToMany(mappedBy = "moyenPaiement")
	@JsonIgnore
	private List<Medecin> medecins;

	public MoyenPaiement() {
		super();
	}

	public MoyenPaiement(String type) {
		super();
		this.type = type;
	}

}
