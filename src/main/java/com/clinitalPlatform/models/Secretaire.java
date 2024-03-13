package com.clinitalPlatform.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "secretaires")
@Data
public class Secretaire{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nom;
	private String prenom;
	private Date dateNaissance;
	private String adresse;

	// in this we create a Bridge table between Cabinet and Secretaire to link them together
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "SecretaireCabinet",
		joinColumns = @JoinColumn(name = "secrt_id",referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "cabinet_id",referencedColumnName = "id_cabinet"))
		private List<Cabinet> cabinet= new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName= "id")
	private User user;

	public Secretaire() {
		super();
	}

	public Secretaire(@NotBlank @Size(max = 50) @Email String email, @NotNull String telephone,
			@NotBlank @Size(max = 120) String password, String nom, String prenom, Date dateNaissance, String adresse,User user) 
			 {
		super();
		this.nom = nom;
		this.prenom = prenom;
		this.dateNaissance = dateNaissance;
		this.adresse = adresse;
		this.user = user;
	}

	

	

}
