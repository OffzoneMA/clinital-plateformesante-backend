package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "assistants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assistant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nom;
	private String prenom;
	private Date dateNaissance;
	private String adresse;
	private String service;

	// in this we create a Bridge table between Cabinet and Assistant to link them together
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "AssistantCabinet",
		joinColumns = @JoinColumn(name = "assist_id",referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "cabinet_id",referencedColumnName = "id_cabinet"))
		private List<Cabinet> cabinet= new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName= "id")
	private User user;

}
