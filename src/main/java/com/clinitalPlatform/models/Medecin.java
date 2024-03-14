package com.clinitalPlatform.models;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.clinitalPlatform.enums.CiviliteEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "medecins")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Medecin {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String matricule_med;
	private String inpe;
	private String nom_med;
	private String prenom_med;
	private String photo_med;
	private String photo_couverture_med;
	private Long StepsValidation;

	@ManyToMany
	private List<ExpertisesMedecin> expertises_med;

	@OneToMany(mappedBy = "medecin")
	private List<DiplomeMedecin> diplome_med;

	private String description_med;
	private String contact_urgence_med;
	
	@Enumerated(value = EnumType.STRING)
	private CiviliteEnum civilite_med;

	@OneToMany(mappedBy = "medecin")
	private List<ExperienceMedecin> experience_med;

	@ManyToOne(fetch = FetchType.EAGER)
	private Ville ville;
	
	@ManyToOne(cascade = CascadeType.ALL)
	private Specialite specialite;

	@OneToMany
	@JsonIgnore
	private List<MoyenPaiement> moyenPaiement;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName= "id")
	@JsonIgnore
	private User user;

	// in this we create a Bridge table between Medecin and Document to link them together
	@ManyToMany
	@JoinTable(name = "DocumentMedecin",
	 	joinColumns = @JoinColumn(name = "medecin_id"),
		inverseJoinColumns = @JoinColumn(name = "document_id"))
		@JsonIgnore
	  private List<Document> Meddoc;

	
	// in this we create a Bridge table between Medecin and DossierMedical to link them together
	@ManyToMany
	@JoinTable(name = "DossierMedecin",
	 	joinColumns = @JoinColumn(name = "medecin_id"),
		inverseJoinColumns = @JoinColumn(name = "dossier_id"))
		@JsonIgnore
	  private List<DossierMedical> Meddossiers;


	// in this we create a Bridge table between Medecin and Cabinet to link them together
	@OneToMany(mappedBy = "medecin",cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	@JsonIgnore
	private List<CabinetMedecinsSpace> cabinets;

	private Boolean isActive;


			@OneToMany(cascade = CascadeType.ALL, mappedBy = "follower")
			@Fetch(FetchMode.JOIN)
			private Set<MedecinNetwork> followers = new HashSet<>();

			@OneToMany(cascade = CascadeType.ALL, mappedBy = "medecin")
			@Fetch(FetchMode.SELECT)
			private Set<MedecinNetwork> following = new HashSet<>();

			@OneToMany(mappedBy = "medecin")
			@JsonIgnore
			private List<Consultation> Consultations;

			@OneToMany(mappedBy = "medecin")
			@JsonIgnore
			private List<Ordonnance> Ordonnance;


	public void removeCabinet(Cabinet cabinet) {
        for (Iterator<CabinetMedecinsSpace> iterator = cabinets.iterator(); 
			iterator.hasNext();) {
            CabinetMedecinsSpace cabinetMed = iterator.next();
 
            if (cabinetMed.getMedecin().equals(this) && cabinetMed.getCabinet().equals(cabinet)) {
                iterator.remove();
                cabinetMed.setMedecin(null);
                cabinetMed.setCabinet(null);
				cabinetMed.setStatus(null);
            }
        }
    }

		public Medecin(Long id, String matricule_med, String inpe, String nom_med, String prenom_med,
				   String photo_med, String photo_couverture_med, List<ExpertisesMedecin> expertises_med, List<DiplomeMedecin> diplome_med,
				   String description_med, String contact_urgence_med,CiviliteEnum civilite_med,
				   List<ExperienceMedecin> experience_med, Ville ville, Specialite specialite,
				   List<MoyenPaiement> moyenPaiement, User user,boolean isActive) {
		this.id = id;
		this.matricule_med = matricule_med;
		this.inpe = inpe;
		this.nom_med = nom_med;
		this.prenom_med = prenom_med;
		this.photo_med = photo_med;
		this.photo_couverture_med = photo_couverture_med;
		this.expertises_med = expertises_med;
		this.diplome_med = diplome_med;
		this.description_med = description_med;
		this.contact_urgence_med = contact_urgence_med;
		this.civilite_med = civilite_med;
		this.experience_med = experience_med;
		this.ville = ville;
		this.specialite = specialite;
		this.moyenPaiement = moyenPaiement;
		this.user = user;
		this.isActive=isActive;
		
	}
}
