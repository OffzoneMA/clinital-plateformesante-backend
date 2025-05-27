package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.RdvStatutEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

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

	@OneToMany(mappedBy = "rendezvous", cascade = CascadeType.ALL, orphanRemoval = true)
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

	@OneToMany(mappedBy = "rdv" ,cascade = CascadeType.ALL, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	@JsonIgnore
	private List<CompteRenduRdv> compteRenduRdv;

	//Metadonnees utiles
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;


	public Rendezvous() {
		super();
	}

	public Rendezvous(DayOfWeek day, String motif, LocalDateTime start, LocalDateTime end,
                      LocalDateTime canceledAt, RdvStatutEnum statut, MotifConsultation motifconsul, Medecin medecin, Boolean iSnewPatient, Patient patient, ModeConsultation modeConsultation, String Commantaire, String LinkVideoCall, Cabinet cabinet) {
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

