package com.clinitalPlatform.models;

import javax.validation.constraints.NotBlank;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "villes")
@Data
public class Ville {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_ville;

	private String nom_ville;

	@ManyToOne(fetch = FetchType.EAGER)
	private Pays pays;

	public Ville() {
		super();
	}

	public Ville(@NotBlank String nom_ville, Pays pays) {
		super();
		this.nom_ville = nom_ville;
		this.pays = pays;
	}


}
