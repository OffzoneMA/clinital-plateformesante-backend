package com.clinitalPlatform.models;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.format.annotation.DateTimeFormat;

import com.clinitalPlatform.enums.RdvStatutEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "rendezvous")
@Data
public class Rendezvous {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "day")
	@Enumerated(value = EnumType.ORDINAL)
	private DayOfWeek day;

	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime start;
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime end;
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime canceledAt;

	@Enumerated(EnumType.STRING)
	private RdvStatutEnum statut;
	private Boolean iSnewPatient;
	private String Commantaire;
	private String LinkVideoCall;
	

	@ManyToOne
	@JoinColumn(name = "medecin", nullable = false, referencedColumnName = "id", insertable = true, updatable = true)
	private Medecin medecin;

	@ManyToOne
	@JoinColumn(name = "patient", nullable = true, referencedColumnName = "id", insertable = true, updatable = true)
	private Patient patient;

	@OneToMany(mappedBy = "rendezvous")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private List<Document> documents;


	@OneToOne(cascade = CascadeType.ALL)

	@JoinColumn(name = "id_mode", referencedColumnName= "id_mode")
	private ModeConsultation modeConsultation;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "id_motif", referencedColumnName= "id_motif")
	private MotifConsultation motifConsultation;

	@ManyToOne
	@JoinColumn(name = "cabinet", nullable = true, referencedColumnName = "id_cabinet", insertable = true, updatable = true)
	private Cabinet cabinet;
	public Rendezvous() {
		super();
	}

	public Rendezvous(DayOfWeek day, @NotBlank String motif, LocalDateTime start, LocalDateTime end,
			LocalDateTime canceledAt, RdvStatutEnum statut,MotifConsultation motifconsul, Medecin medecin,Boolean iSnewPatient, Patient patient,ModeConsultation modeConsultation,String Commantaire,String LinkVideoCall,Cabinet cabinet) {
		super();
		this.day = day;
		this.start = start;
		this.end = end;
		this.canceledAt = canceledAt;
		this.statut = statut;
		this.medecin = medecin;
		this.patient = patient;
		this.motifConsultation= motifconsul;
		this.modeConsultation=modeConsultation;
		this.iSnewPatient=iSnewPatient;
		this.Commantaire=Commantaire;
		this.LinkVideoCall=LinkVideoCall;
		this.cabinet=cabinet;
	}

}
