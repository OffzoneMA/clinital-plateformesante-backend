package com.clinitalPlatform.models;

import java.util.List;

import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

	public MotifConsultation(Long motif) {
		this.id_motif=motif;
	}
}



    

