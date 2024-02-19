package com.clinitalPlatform.models;


import com.clinitalPlatform.enums.CiviliteEnum;
import com.clinitalPlatform.enums.DemandeStateEnum;

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
	
	@Column(name="matricule_medecin")
	private String matricule;
	
	@Column(name="mail_medecin")
	private String mail;
	
	@Column(name="specialite_medecin")
	private String specialite;
	
	@Column(name="inpe")
	private String inpe;
	
	@Column(name="nom_cabinet")
	private String nom_cab;

	@Column(name="adresse_cabinet")
	private String adresse;
	
	@Column(name="code_postal_cabinet")
	private String code_postal;

	@Column(name="validation")//l'etat du demande si valider ou pas :
	@Enumerated(EnumType.STRING)
	private DemandeStateEnum validation;

	@Enumerated(value = EnumType.STRING)
	private CiviliteEnum civilite_med;

}
