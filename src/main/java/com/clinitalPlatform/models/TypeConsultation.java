package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "type_consultation")
@Data
public class TypeConsultation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "consultation_id")
	private Long consultationId;

	@Column(name = "title")
	private String title;

	@Column(name = "tarif")
	private double tarif;

	@ManyToOne
	@JoinColumn(name = "medecin_id", referencedColumnName= "id")
	private Medecin medecin;

	public TypeConsultation(){
		super();
	}



}
