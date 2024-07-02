package com.clinitalPlatform.models;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.format.annotation.DateTimeFormat;

import com.clinitalPlatform.enums.ConsultationPeriodEnum;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "medecin_schedule")
@Entity
public class MedecinSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "day")
	@Enumerated(value = EnumType.ORDINAL)
	private DayOfWeek day;

	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime availabilityStart;
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime availabilityEnd;

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "modeschedules", 
	joinColumns = { @JoinColumn(name = "sched_id", referencedColumnName   ="id") },
	inverseJoinColumns = { @JoinColumn(name = "mode_id",referencedColumnName="id_mode") })
	private List<ModeConsultation> modeconsultation;

	@ManyToMany(cascade=CascadeType.MERGE)
	@JoinTable(name = "motif_consultations_schedules",
	 	joinColumns = @JoinColumn(name = "id_sched",referencedColumnName   ="id"),
		inverseJoinColumns = @JoinColumn(name = "id_motif",referencedColumnName   ="id_motif"))
	private List<MotifConsultation> motifConsultation;
	 
	@Column(name = "period")
	@Enumerated(EnumType.STRING)
	private ConsultationPeriodEnum period;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "medecin_id", nullable = false, referencedColumnName = "id", insertable = true, updatable = true)
	@JsonIgnore
	private Medecin medecin;


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_cabinet", nullable = false, referencedColumnName = "id_cabinet", insertable = true, updatable = true)
	//@JsonIgnore
	@JsonManagedReference
	private Cabinet cabinet;

	private Boolean isnewpatient;
	
	public MedecinSchedule(){
		super();
	}
	
}
