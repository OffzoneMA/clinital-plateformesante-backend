package com.clinitalPlatform.models;


import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.enums.DemandeStateEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "demande")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Demande {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@Column(name="nom_medecin")
	private String nom_med;
	
	@Column(name="prenom_medecin")
	private String prenom_med;
	
	@Column(name="state")
	private int state;
	
	@Column(name="mail_medecin")
	private String mail;
	
	@Column(name="specialite_medecin")
	private String specialite;
	
	@Column(name="inpe")
	private String inpe;
	

	@Column(name="ville")
	private String ville;
	
	@Column(name="phonenumber")
	private String phonenumber;
	
	@OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	@JsonIgnore
	private User user;

	@Column(name="validation")//l'etat du demande si valider ou pas :
	@Enumerated(EnumType.STRING)
	private DemandeStateEnum validation;

	@Enumerated(value = EnumType.STRING)
	private CiviliteEnum civilite_med;

	//Métadonnées utiles
	@Column(name = "created_at")
	private java.time.LocalDateTime createdAt;

	@Column(name = "updated_at")
	private java.time.LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = java.time.LocalDateTime.now();
		this.updatedAt = java.time.LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = java.time.LocalDateTime.now();
	}

}
