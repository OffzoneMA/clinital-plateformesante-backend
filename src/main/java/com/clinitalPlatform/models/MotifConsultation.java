package com.clinitalPlatform.models;

import java.util.List;

import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "motifs_consultation")
@Data
public class MotifConsultation {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_motif;
    @Column(name = "libelle")
	@Enumerated(EnumType.STRING)
	private MotifConsultationEnum motif;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "motifConsultation")
    @JsonIgnore
  	private List<MedecinSchedule> Schedules;
	
	public MotifConsultation() {
		super();
	}

	public MotifConsultation(MotifConsultationEnum motif) {
		super();
		this.motif = motif;
	}
}



    

