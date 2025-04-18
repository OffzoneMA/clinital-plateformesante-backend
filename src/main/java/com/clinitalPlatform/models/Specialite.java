package com.clinitalPlatform.models;

import java.util.List;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "specialites")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Specialite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_spec;

	private String libelle;


	private boolean valid = true;


	@OneToMany(mappedBy="specialite")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnore
    private List<Medecin> medecins;

	public Specialite(Long id_spec, String libelle, List<Medecin> medecins , boolean valid) {
		this.id_spec = id_spec;
		this.libelle = libelle;
		this.medecins = medecins;
		this.valid = valid;
	}

	public Specialite() {
		super();
	}

	
}
