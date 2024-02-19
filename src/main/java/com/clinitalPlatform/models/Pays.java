package com.clinitalPlatform.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pays")
@Data
public class Pays {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_pays;

	private String nom_pays;

	public Pays( String nom_pays) {
		super();
		this.nom_pays = nom_pays;
	}

	public Pays() {
		super();
	}

}
