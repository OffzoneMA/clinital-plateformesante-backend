package com.clinitalPlatform.models;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "cabinet")
@Data
public class Cabinet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_cabinet;
	private String nom;
	private String adresse;
	private String code_post;
	private String phoneNumber;

	@ManyToOne(cascade = CascadeType.ALL)
	private Ville ville;

	@OneToMany(cascade = CascadeType.ALL,mappedBy = "cabinet",fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	@JsonIgnore
	private List<DocumentsCabinet> documentsCabinets;

	@OneToMany(cascade = CascadeType.ALL,mappedBy = "cabinet",fetch = FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	@JsonIgnore
	private List<CabinetMedecinsSpace> medecin;
	
	@ManyToMany(mappedBy = "cabinet")
	@JsonIgnore
	private List<Secretaire> secretaire;

	@ManyToOne(cascade = CascadeType.ALL)
	@JsonIgnore
	private Medecin creator;

	private Boolean state;
	public Cabinet() {
		super();
	}

	public Cabinet(@NotNull String nom, @NotNull String adresse, @NotNull String code_post,String phoneNumber, @NotNull Ville ville,Medecin creator,Boolean state
			) {
		super();
		this.nom = nom;
		this.adresse = adresse;
		this.code_post = code_post;
		this.phoneNumber=phoneNumber;
		this.ville=ville;
		this.creator=creator;
		this.state=state;
	
	}

}
