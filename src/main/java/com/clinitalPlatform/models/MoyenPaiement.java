package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "moyenspaiement")
@Data
public class MoyenPaiement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_mp;
	private String type;

	public MoyenPaiement() {
		super();
	}

	public MoyenPaiement(String type) {
		super();
		this.type = type;
	}

}
