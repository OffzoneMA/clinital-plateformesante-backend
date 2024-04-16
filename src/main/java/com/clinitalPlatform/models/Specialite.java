package com.clinitalPlatform.models;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "specialites")
@Data
public class Specialite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_spec;

	private String libelle;

	@OneToMany(mappedBy="specialite")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnore
    private List<Medecin> medecins;

	public Specialite(Long id_spec, String libelle, List<Medecin> medecins) {
		this.id_spec = id_spec;
		this.libelle = libelle;
		this.medecins = medecins;
	}

	public Specialite() {
		super();
	}

	

}
