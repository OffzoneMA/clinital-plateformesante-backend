package com.clinitalPlatform.models;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "cabinet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cabinet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_cabinet;
	private String nom;
	private String adresse;
	private String code_post;
	private String phoneNumber;
	private Double longitude;
	private Double latitude;

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

	@OneToOne(cascade = CascadeType.ALL)
	//@JsonIgnore
	@JsonManagedReference
	private PaymentInfo paymentInfo;

	//Métadonnées utiles
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Cabinet(@NotNull String nom, @NotNull String adresse, @NotNull String code_post,String phoneNumber, @NotNull Ville ville,Medecin creator,Boolean state,@NotNull PaymentInfo paymentInfo
	) {
		super();
		this.nom = nom;
		this.adresse = adresse;
		this.code_post = code_post;
		this.phoneNumber=phoneNumber;
		this.ville=ville;
		this.creator=creator;
		this.state=state;
		this.paymentInfo=paymentInfo;

	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

}
