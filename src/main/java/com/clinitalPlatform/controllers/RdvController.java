package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.ModeCountDTO;
import com.clinitalPlatform.dto.PatientCountsDTO;
import com.clinitalPlatform.dto.RendezvousDTO;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.RendezvousRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.PatientResponse;
import com.clinitalPlatform.payload.response.RendezvousResponse;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.services.*;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@org.springframework.transaction.annotation.Transactional
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/rdv")
public class RdvController {

	@Autowired
	RdvRepository rdvrepository;

	@Autowired
	RendezvousService rdvservice;

	@Autowired
	ClinitalModelMapper mapper;

	@Autowired
	MedecinRepository medRepo;

	@Autowired
	TypeConsultationRepository typeConsultRepo;

	@Autowired
	MedecinScheduleRepository medScheduleRepo;

	@Autowired
	PatientRepository patientRepo;

	@Autowired
	MedecinServiceImpl medecinService;

	@Autowired
	PatientService patientService;

	@Autowired
	DocumentRepository docrepository;
  
	@Autowired
	SpecialiteRepository speciarepspo;

	@Autowired
	ModeConsultRespository moderespo;

	@PersistenceContext
	private EntityManager entityManger;

	@Autowired
	GlobalVariables globalVariables;

	@Autowired
	private ActivityServices activityServices;

    @Autowired
    PushNotificationService pushNotificationService;

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@GetMapping("/rdv-byId/{id}")
	public ResponseEntity<?> getRdvById(@PathVariable Long id) throws NotFoundException {
		try {
			Rendezvous rdv = rdvrepository.getById(id);
            activityServices.createActivity(new Date(), "Read", "Show rdv ID : " + id, globalVariables.getConnectedUser());
			LOGGER.info("Show rdv ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(mapToRendezvousResponse(rdv));
		} catch (Exception e) {
			LOGGER.error("Error fetching RDV by ID", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("medcin/patientId/{id}")
	//@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public ResponseEntity<Set<Medecin>> findMedecinsWithRendezvousForPatient(@PathVariable Long id) {
	    try {
	        Long userId = globalVariables.getConnectedUser().getId();
	        List<Rendezvous> rendezvousList = rdvrepository.findRdvByIduserandPatient(userId, id);

	        activityServices.createActivity(new Date(), "Read", "Consulting medcins by Patient by ID : " + id,
	                userId);
	        LOGGER.info("Consulting medcins by Patient by ID: " + id + ", UserID : "
	                + globalVariables.getConnectedUser().getId());

	        if (rendezvousList.isEmpty()) {
	            return ResponseEntity.ok(null); // Aucun rendez-vous pour ce patient
	        }

	        // Utiliser un ensemble pour stocker les médecins (pas de doublons)
	        Set<Medecin> medecins = new HashSet<>();
	        for (Rendezvous rdv : rendezvousList) {
	            Medecin medecin = rdv.getMedecin(); // Obtenir le médecin associé au rendez-vous
	            medecins.add(medecin);
	        }

	        return ResponseEntity.ok(medecins);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}

	@GetMapping("medcin/findByPatients")
	//@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public ResponseEntity<Set<Medecin>> findMedecinsWithRendezvousForPatients(@RequestParam List<Long> patientIds) {
		try {
			Long userId = globalVariables.getConnectedUser().getId();

			// Utilisation d'un ensemble pour éviter les doublons
			Set<Medecin> medecins = new HashSet<>();

			// Parcourir chaque ID de patient et récupérer les rendez-vous
			for (Long patientId : patientIds) {
				List<Rendezvous> rendezvousList = rdvrepository.findRdvByIduserandPatient(userId, patientId);

				rendezvousList.forEach(rdv -> medecins.add(rdv.getMedecin()));

				activityServices.createActivity(new Date(), "Read",
						"Consulting medecins by Patient ID: " + patientId, userId);
				LOGGER.info("Consulting medecins by Patient ID: " + patientId + ", UserID: " + userId);
			}

			return ResponseEntity.ok(medecins);

		} catch (Exception e) {
			LOGGER.error("An error occurred while finding medecins for patients.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptySet());
		}
	}

//	@Value(value = "${azure.storage.account-key}")
//	String azureStorageToken;

	// Get all RDVs : a revoire ....
	@GetMapping("rdvs")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	Iterable<RendezvousResponse> rendezvous() {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		try {
			activityServices.createActivity(new Date(), "Read", "Consulting All Rdv  ",
					globalVariables.getConnectedUser());
			LOGGER.info("Consulting All Rdv, UserID : " + globalVariables.getConnectedUser().getId());
			return rdvrepository.findAll(globalVariables.getConnectedUser().getId()).stream()
					.map(rdv -> mapper.map(rdv, RendezvousResponse.class)).collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Get Rdv By Id and Id patient : %OK%
	public RendezvousResponse mapToRendezvousResponse(Rendezvous rdv) {
		RendezvousResponse response = new RendezvousResponse();
		response.setId(rdv.getId());
		response.setDay(rdv.getDay() != null ? rdv.getDay().name() : null); // Convertit DayOfWeek en chaîne
		response.setStart(rdv.getStart());
		response.setEnd(rdv.getEnd());
		response.setCanceledat(rdv.getCanceledAt());
		response.setStatut(rdv.getStatut());
		response.setModeconsultation(rdv.getModeConsultation()); // Conserve l'objet complet ou convertissez-le si nécessaire
		response.setMedecinid(rdv.getMedecin() != null ? rdv.getMedecin().getId() : null);
		response.setPatientid(rdv.getPatient() != null ? rdv.getPatient().getId() : null);
		response.setLinkVideoCall(rdv.getLinkVideoCall());
		response.setCabinetid(rdv.getCabinet() != null ? rdv.getCabinet().getId_cabinet(): null);
		response.setMotifid(rdv.getMotifConsultation().getId_motif());

		// Conversion de motif
		if (rdv.getMotifConsultation() != null) {
			response.setMotif(rdv.getMotifConsultation().getMotif()); // Utilisation directe de l'énumération
		} else {
			response.setMotif(null); // ou une valeur par défaut si souhaité
		}

		if(rdv.getPatient() != null) {
			response.setPatient(mapper.map(rdv.getPatient(), PatientResponse.class));
		}

		return response;
	}

	@GetMapping("patient/rdvById/{id}")
	//@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public ResponseEntity<?> getRdvByIdBypatient(@PathVariable Long id) throws Exception {
		try {
			Patient pat = patientService.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());
			if (pat == null) {
				LOGGER.warn("No patient found for connected user with ID: " + globalVariables.getConnectedUser().getId());
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponse(false, "Patient not found for the connected user"));
			}

			Rendezvous isRdv = rdvrepository.findRdvByIdUserandId(globalVariables.getConnectedUser().getId() , id);

			if (isRdv != null) {
				RendezvousResponse rdvResponse = mapToRendezvousResponse(isRdv); // Utiliser la méthode de mappage
				activityServices.createActivity(new Date(), "Read", "Show rdv ID : " + id, globalVariables.getConnectedUser());
				LOGGER.info("Show rdv ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
				return ResponseEntity.ok(rdvResponse); // Retourner le DTO
			} else {
				activityServices.createActivity(new Date(), "Warning", "Cannot find Rdv By ID : " + id, globalVariables.getConnectedUser());
				LOGGER.warn("Cannot find Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "RDV Not Found " + id));
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching RDV by ID", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/*@GetMapping("patient/rdvById/{id}")
	@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public ResponseEntity<?> getRdvByIdBypatient(@PathVariable Long id) throws Exception {
		Patient pat = patientService.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());
		try {
			Optional<Rendezvous> isRdv = rdvrepository.findRdvByIdandPatient(id, pat.getId());
			if (isRdv.isPresent()) {

				Rendezvous rdv = rdvrepository.findRdvByIdandPatient(id, pat.getId()).get();

				activityServices.createActivity(new Date(), "Read", "Show rdv  ID : " + id,
						globalVariables.getConnectedUser());
				LOGGER.info("Show rdv ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
				return ResponseEntity.ok(mapper.map(rdv, RendezvousResponse.class));
			} else
				activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
						globalVariables.getConnectedUser());
			LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found " + id));

			} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(null);

        }
    }*/

	@GetMapping("/patient/rdvByIdMedecin")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	@ResponseBody
	public List<RendezvousResponse> findRdvByIdMedecin(@RequestParam Long id) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();

		Medecin medecin = medRepo.findById(id).orElseThrow(() -> new Exception("No Matching Found"));
		activityServices.createActivity(new Date(), "Read", "Consulting rdv by Medecin ID : " + id,
				globalVariables.getConnectedUser());
		LOGGER.info(
				"Consulting Rdv  by Medecin ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
		return rdvrepository.getRdvByIdMedecin(medecin.getId(), globalVariables.getConnectedUser().getId()).stream()
				.map(rdv -> mapper.map(rdv, RendezvousResponse.class))
				.collect(Collectors.toList());
	}

	@GetMapping("/patient/rdvbypatient/{id}")
	public ResponseEntity<?> findRdvByPatient(@PathVariable Long id) throws Exception {
		try {
			Optional<Patient> pat = patientService.findById(id);
			Patient patient = null;
			if (pat.isPresent()) {
				activityServices.createActivity(new Date(), "Read", "Consulting rdv by Patient ID : " + id,
						globalVariables.getConnectedUser());
				patient = pat.get();
			}

			if (patient != null) {
                LOGGER.info("Consulting Rdv  by Patient ID : {}, UserID : {}", id, globalVariables.getConnectedUser().getId());
				List<Rendezvous> rendezvousList = rdvrepository.findAllRdvByPatient(patient.getId());

				activityServices.createActivity(new Date(), "Read", "Consulting rdv by Patient ID : " + id,
						globalVariables.getConnectedUser());
				return ResponseEntity.ok(rendezvousList);
			} else {
                LOGGER.warn("No patient found with ID: {}", id);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Patient not found"));
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching RDV by Patient ID", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}


	// Get RDV By patient Name : %OK% getRdvByNomPatientByMedecin
	@GetMapping("patient/rdvByNomPatient/{nompat}")
	@ResponseBody
	//@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public List<Rendezvous> findRdvByNomPatient(@PathVariable(value = "nompat") @NotNull String nomPatient) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		activityServices.createActivity(new Date(), "Read",
				"Consulting rdv for Patient by Patient Name : " + nomPatient, globalVariables.getConnectedUser());
		LOGGER.info("Consulting Rdv for Patient by Patient Name : " + nomPatient + ", UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvrepository.getRdvByNomPatient(nomPatient, globalVariables.getConnectedUser().getId()).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class)).collect(Collectors.toList());
	}

	// a revoire
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	@GetMapping("/patient/rdvByDate")
	@ResponseBody
	public List<RendezvousResponse> rdvByDate(
			@Valid @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) throws Exception {
		// UserDetailsImpl uDetailsImpl= (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		activityServices.createActivity(new Date(), "Read", "Consulting rdv for Patient by Date : " + jour,
				globalVariables.getConnectedUser());
		LOGGER.info("Consulting Rdv for Patient by Date : " + jour + ", UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvrepository.getRdvByDate(jour, globalVariables.getConnectedUser().getId()).stream()
				.map(rdv -> mapper.map(rdv, RendezvousResponse.class))
				.collect(Collectors.toList());
	}

	// @GetMapping("/rdvByMotif")
	// @ResponseBody
	// public List<RendezvousResponse> findRdvByMotif(@Valid @RequestBody
	// RendezvousDTO rdvDetails) {
	// return rdvrepository.getRdvByMotif(rdvDetails.getMotif()).stream()
	// .map(rdv -> mapper.map(rdv,
	// RendezvousResponse.class)).collect(Collectors.toList());
	// }

	// ADD an RDV by Medecin : %OK%

	@PreAuthorize("hasAnyRole('ROLE_MEDECIN' , 'ROLE_ADMIN' , 'ROLE_SECRETAIRE')")
	@PostMapping("patient/addRdvgestion")
	@ResponseBody
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public ResponseEntity<?> addRendezvousgesttion(@Valid @RequestBody RendezvousDTO c)
			throws Exception {
		try {

			System.err.println(c);
			Medecin medecin = medRepo.findById(c.getMedecinid())
					.orElseThrow(() -> new BadRequestException("Medecin not found for this id ::" + c.getMedecinid()));
			Patient patient = patientRepo.findById(c.getPatientid()).orElseThrow(
					() -> new BadRequestException("Patient not found for this id :: " +
							c.getPatientid()));

			return ResponseEntity.ok(rdvservice.AddnewRdv(globalVariables.getConnectedUser(), c, medecin, patient));

		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}
	}

	// ADD an RDV by Patient : %OK%
	@PreAuthorize("hasAnyRole('ROLE_MEDECIN' , 'ROLE_PATIENT' , 'ROLE_SECRETAIRE')")
	@PostMapping("patient/addRdv")
	@ResponseBody
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public ResponseEntity<?> addRendezvousByPatient(@Valid @RequestBody RendezvousDTO c)
			throws Exception {

		try {

			System.err.println(c);
			Medecin medecin = medRepo.findById(c.getMedecinid())
					.orElseThrow(() -> new BadRequestException("Medecin not found for this id ::" + c.getMedecinid()));
			Patient patient = patientRepo.findById(c.getPatientid()).orElseThrow(
					() -> new BadRequestException("Patient not found for this id :: " +
							c.getPatientid()));

			return ResponseEntity.ok(rdvservice.AddnewRdv(globalVariables.getConnectedUser(), c, medecin, patient));

		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}
	}

	@PutMapping("/patient/updateRdv/{id}")
	public ResponseEntity<Rendezvous> updateeRDV(@PathVariable("id") Long rdvId, @RequestBody RendezvousDTO rdvDTO) throws Exception {
		try{
			Medecin medecin = medRepo.findById(rdvDTO.getMedecinid())
					.orElseThrow(() -> new BadRequestException("Medecin not found for this id ::" + rdvDTO.getMedecinid()));
			Patient patient = patientRepo.findById(rdvDTO.getPatientid()).orElseThrow(
					() -> new BadRequestException("Patient not found for this id :: " +
							rdvDTO.getPatientid()));
			Rendezvous updatedRDV = rdvservice.updateerdv(rdvId, rdvDTO,medecin,patient);
			return ResponseEntity.ok(updatedRDV);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}}

    // DELETE AN RDV By Medecin : %ok%
    @PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
    @DeleteMapping("/patient/delete/{id}")
    public ResponseEntity<ApiResponse> deleteRdvbyPatient(@Valid @PathVariable Long id) {
         try {
             User connectedUser = globalVariables.getConnectedUser();
			 if (connectedUser == null) {
				 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						 .body(new ApiResponse(false, "User not authenticated"));
			 }

             Rendezvous rdv = rdvrepository.getById(id);

             if (rdv == null) {
                 activityServices.createActivity(new Date(), "Warning",
                         "Cannot find Rdv By ID : " + id, globalVariables.getConnectedUser());

                 LOGGER.warn("Cannot find Rdv By ID : {}, UserID : {}",
                         id, globalVariables.getConnectedUser().getId());

                 return ResponseEntity.status(HttpStatus.NOT_FOUND)
                         .body(new ApiResponse(false, "Rendez-vous non trouvé pour id :: " + id));
             }

             rdvservice.deleteRendezvous(id);

             activityServices.createActivity(new Date(), "Delete",
                     "Patient Delete Rdv By : " + id, globalVariables.getConnectedUser());

             LOGGER.info("Patient Delete Rdv By ID : {}, UserID : {}",
                     id, globalVariables.getConnectedUser().getId());

             return ResponseEntity.ok(new ApiResponse(true, "RDV has been deleted successfully"));

         } catch (Exception e) {
             LOGGER.error("Error deleting Rdv: {}", e.getMessage(), e);

             activityServices.createActivity(new Date(), "Error",
                     "Error deleting Rdv: " + e.getMessage(), id);

             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body(new ApiResponse(false, "An error occurred while deleting the Rendez-vous"));
         }
     }

	// Update RDv bay Medecin : %ok%
	// Get Rdv For connected Medecin : %OK%
	// Get Rdv For connected Patient : %OK%
	@GetMapping("/rdvs/patient")
	List<Rendezvous> rendezvousForPatient() throws Exception {
		// Retrieve the list of patients by user ID
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());

		if (patients.isEmpty()) {
			throw new Exception("No patients found for user ID: " + globalVariables.getConnectedUser().getId());
		}

		List<Rendezvous> allRdv = new ArrayList<>();

		// Iterate over each patient and collect their appointments
		for (Patient patient : patients) {
			Long patientId = patient.getId();
			List<Rendezvous> rdvpatient = rdvrepository.findAllRdvByPatient(patientId).stream()
					.map(rdv -> mapper.map(rdv, Rendezvous.class)) // Assuming 'mapper' converts RendezvousEntity to Rendezvous
					.collect(Collectors.toList());
			allRdv.addAll(rdvpatient);
		}

		// Log activity
		activityServices.createActivity(new Date(), "Read", "Show All Rdv for Patients", globalVariables.getConnectedUser());
		LOGGER.info("Show All Rdv for Patients, UserID: " + globalVariables.getConnectedUser().getId());

		return allRdv;
	}

	/*
	 * @GetMapping("/rdvs/secretaire/{idsec}")
	 * Iterable<RendezvousResponse> rendezvousForSecretaire(@PathVariable(value =
	 * "idsec") Long idsec) {
	 * return rdvrepository.findByMedecinCabinetSecretairesId(idsec).stream()
	 * .map(rdv -> mapper.map(rdv,
	 * RendezvousResponse.class)).collect(Collectors.toList());
	 * }
	 */
	// cancel RDV for connected Medecin : %OK% les autres status.

	// cancel Rdv For connected Patient : %OK%
	@PutMapping("/patient/cancelRdv/{id}")
	//@PreAuthorize("hasAnyRole('ROLE_PATIENT'  )")
	public ResponseEntity<?> cancelRdvByPatient(@Valid @PathVariable Long id) throws Exception {
		Patient pat = patientService.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());

		if(pat == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Patient not found by userId : " + globalVariables.getConnectedUser().getId()));
		}

		Rendezvous rdv = rdvrepository.findRdvByIdUserandId(globalVariables.getConnectedUser().getId(), id);
		if (rdv != null) {

			rdv.setCanceledAt(LocalDateTime.now());
			rdv.setStatut(RdvStatutEnum.ANNULE);
			Rendezvous updatedrdv = rdvrepository.save(rdv);
			activityServices.createActivity(new Date(), "Update", "Patient Cancel Rdv ID : " + id,
					globalVariables.getConnectedUser());
			LOGGER.info("Patient Cancel Rdv ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
            pushNotificationService.sendAppointmentCancellation(
                    rdv.getPatient().getUser().getId(), rdv.getMedecin().getSpecialite().getLibelle() ,
                    "Votre rendez-vous du " + rdv.getStart().toLocalDate() + " a été annulé." ,
                    "Dr" + " " + rdv.getMedecin().getNom_med() + " " + rdv.getMedecin().getPrenom_med() ,
                    rdv.getStart() , updatedrdv.getId() , rdv.getMedecin().getId() , rdv.getPatient().getId()
            );

			LOGGER.info("cancel rdv" + updatedrdv.getId());
			return ResponseEntity.ok(mapper.map(updatedrdv, RendezvousResponse.class));
		} else
			activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
					globalVariables.getConnectedUser());
		LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
		return ResponseEntity.badRequest().body(new ApiResponse(false, "RDV Not Found" + id));

	}

	@PutMapping("/medecin/cancelRdv/{id}")
	//@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN')")
	public ResponseEntity<?> cancelRdvByMedecin(@Valid @PathVariable Long id) throws Exception {
		Medecin medecin = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		if(medecin == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Medecin not found by userId : " + globalVariables.getConnectedUser().getId()));
		}

		Rendezvous rdv = rdvrepository.getById(id);
        rdv.setCanceledAt(LocalDateTime.now());
        rdv.setStatut(RdvStatutEnum.ANNULE);
        Rendezvous updatedrdv = rdvrepository.save(rdv);
        activityServices.createActivity(new Date(), "Update", "Medecin Cancel Rdv ID : " + id,
                globalVariables.getConnectedUser());
        LOGGER.info("Medecin Cancel Rdv ID : {}, UserID : {}", id, globalVariables.getConnectedUser().getId());
        pushNotificationService.sendAppointmentCancellation(
                rdv.getMedecin().getUser().getId() , rdv.getMedecin().getSpecialite().getLibelle() ,
                "Votre rendez-vous du " + rdv.getStart().toLocalDate() + " a été annulé." ,
                "Dr" + " " + rdv.getMedecin().getNom_med() + " " + rdv.getMedecin().getPrenom_med() ,
                rdv.getStart() , updatedrdv.getId() , rdv.getMedecin().getId() , rdv.getPatient().getId()
        );
        return ResponseEntity.ok(mapper.map(updatedrdv, RendezvousResponse.class));

    }


	@PutMapping("/patient/cancelRdvById/{id}")
	//@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN')")
	public ResponseEntity<?> cancelRdvById(@Valid @PathVariable Long id) throws Exception {

		Rendezvous rdv = rdvrepository.getById(id);
        rdv.setCanceledAt(LocalDateTime.now());
        rdv.setStatut(RdvStatutEnum.ANNULE);
        Rendezvous updatedrdv = rdvrepository.save(rdv);
        activityServices.createActivity(new Date(), "Update", "Patient Cancel Rdv ID : " + id,
                globalVariables.getConnectedUser());
        LOGGER.info("Patient Cancel Rdv ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
        pushNotificationService.sendAppointmentCancellation(
                rdv.getPatient().getUser().getId(), rdv.getMedecin().getSpecialite().getLibelle() ,
                "Votre rendez-vous du " + rdv.getStart().toLocalDate() + " a été annulé." ,
                "Dr" + " " + rdv.getMedecin().getNom_med() + " " + rdv.getMedecin().getPrenom_med() ,
                rdv.getStart() , updatedrdv.getId() , rdv.getMedecin().getId() , rdv.getPatient().getId()
        );
        LOGGER.info("cancel rdv" + updatedrdv.getId());
        return ResponseEntity.ok(mapper.map(updatedrdv, RendezvousResponse.class));

    }

	// CHANGE RDV Status for connected Medecin : %OK% les autres status.
	@PutMapping("/patient/changestatu/{id}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	public ResponseEntity<?> ChangeRdvSttByPatient(@Valid @PathVariable Long id,
			@Valid @RequestBody RendezvousRequest requestrdv) throws Exception {
		// Rendezvous rdv = rdvrepository.findById(id)
		// .orElseThrow(() -> new BadRequestException("Rendez-vous not found for this id
		// :: " + id));
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Patient patient = patientRepo.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());

		Optional<Rendezvous> isRdv = rdvrepository.findRdvByIdandPatient(id, patient.getId());
		if (isRdv.isPresent()) {

			Rendezvous rdv = rdvrepository.findRdvByIdandMedecin(id, patient.getId()).get();
			rdv.setStatut(requestrdv.getStatut());
			final Rendezvous updatedrdv = rdvrepository.save(rdv);
			activityServices.createActivity(new Date(), "Update",
					"Medecin Change Rdv Statue ID : " + id + " to " + requestrdv.getStatut(),
					globalVariables.getConnectedUser());
			LOGGER.info("Medecin Change Rdv Statue ID : " + id + " to " + requestrdv.getStatut() + ", UserID : "
					+ globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(mapper.map(updatedrdv, Rendezvous.class));
		} else
			activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
					globalVariables.getConnectedUser());
		LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
		return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found" + id));

	}

	// RDV By DATE FELTRING (day,week, month ,year) :

	/*
	 * Patient : RDV FOR Patient BY DAY :
	 */
	@GetMapping("/patient/rdvbyday/{day}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_ADMIN')")
	List<Rendezvous> rendezvousPatientByday(@Valid @PathVariable long day) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());

		List<Rendezvous> rdvpatient = null;
		for (Patient pat : patients) {

			rdvpatient = rdvrepository.getRendezvousByDay(day).stream()
					.map(rdv -> mapper.map(rdv, Rendezvous.class))
					.collect(Collectors.toList());
		}
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient By Month : " + day,
				globalVariables.getConnectedUser());
		LOGGER.info("Consult All Rdv for My patients by day, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvpatient;
	}
	// Get Rdv For connected Patient : %OK%

	// RDV FOR Patient BY WEEK :
	@GetMapping("/patient/rdvbyweek/{week}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	List<Rendezvous> rendezvousPatientByweek(@Valid @PathVariable long week) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());

		List<Rendezvous> rdvpatient = null;
		for (Patient pat : patients) {

			rdvpatient = rdvservice.getRdvMedByWeek(week, pat.getId()).stream()
					.map(rdv -> mapper.map(rdv, Rendezvous.class))
					.collect(Collectors.toList());
		}
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient By week : " + week,
				globalVariables.getConnectedUser());
		LOGGER.info("Consult All Rdv for My patients by week, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvpatient;
	}

	// RDV FOR Patient BY MONTH :
	@GetMapping("/patient/rdvbymonth/{month}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	List<Rendezvous> rendezvousPatientBymonth(@Valid @PathVariable long month) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());

		List<Rendezvous> rdvpatient = null;
		for (Patient pat : patients) {

			rdvpatient = rdvservice.getRdvPatientByMonth(month, pat.getId()).stream()
					.map(rdv -> mapper.map(rdv, Rendezvous.class))
					.collect(Collectors.toList());
		}
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient By Month : " + month,
				globalVariables.getConnectedUser());
		LOGGER.info("Consult All Rdv for My patients by Month, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvpatient;
	}

	// RDV FOR Patient BY YEAR :
	@GetMapping("/patient/rdvbyyear/{year}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	List<Rendezvous> rendezvousPatientByyear(@Valid @PathVariable long year) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());

		List<Rendezvous> rdvpatient = null;
		for (Patient pat : patients) {

			rdvpatient = rdvservice.getRdvMedByYear(year, pat.getId()).stream()
					.map(rdv -> mapper.map(rdv, Rendezvous.class))
					.collect(Collectors.toList());
		}
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient By Year : " + year,
				globalVariables.getConnectedUser());
		LOGGER.info("Consult All Rdv for My patients by Year, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvpatient;
	}

	/*
	 * Patient : RDV FOR Patient BY DAY :
	 * and id patient
	 */
	@GetMapping("/patient/rdvbyday/{id}/{day}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	List<Rendezvous> rdvforSpecificPatientByday(@Valid @PathVariable long day,
			@Valid @PathVariable long id) throws Exception {
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient ID : " + id + " By Day ",
				globalVariables.getConnectedUser());
		LOGGER.info("Consult Rdv for Patient ID : " + id + " By Day , UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByDay(day, id).stream().map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	// RDV FOR Patient BY WEEK :and id patient
	@GetMapping("/patient/rdvbyweek/{id}/{week}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	List<Rendezvous> rdvforSpecificPatientByweek(@Valid @PathVariable long week,
			@Valid @PathVariable long id) throws Exception {
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient ID : " + id + " By Week ",
				globalVariables.getConnectedUser());
		LOGGER.info("Consult Rdv for Patient ID : " + id + " By Week , UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByWeek(week, id).stream().map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	// RDV FOR Patient BY MONTH and id patient:
	@GetMapping("/patient/rdvbymonth/{id}/{month}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	List<Rendezvous> rdvforSpecificPatientBymonth(@Valid @PathVariable long month,
			@Valid @PathVariable long id) throws Exception {
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient ID : " + id + " By Month ",
				globalVariables.getConnectedUser());
		LOGGER.info("Consult Rdv for Patient ID : " + id + " By Month , UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByMonth(month, id).stream().map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	// RDV FOR Patient BY YEAR :and id patient
	@GetMapping("/patient/rdvbyyear/{id}/{year}")
	@PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_MEDECIN' , 'ROLE_ADMIN')")
	List<Rendezvous> rdvforSpecificPatientByyear(@Valid @PathVariable long year,
			@Valid @PathVariable long id) throws Exception {
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient ID : " + id + " By Year ",
				globalVariables.getConnectedUser());
		LOGGER.info("Consult Rdv for Patient ID : " + id + " By Year , UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByYear(year, id).stream().map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	// Change start date of a RDV.
	@PostMapping("/MoveRdv/{id}")
	@ResponseBody
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	//@PreAuthorize("hasAuthority('ROLE_PATIENT')")
	public ResponseEntity<?> MoveRDV(@Valid @RequestBody RendezvousRequest rdvDetails, @Valid @PathVariable(value = "id") long idrdv) throws BadRequestException {
		try {
			Rendezvous isRdv = rdvrepository.getById(idrdv);
			if (isRdv != null) {
				// update
				return ResponseEntity.ok(rdvservice.UpdateRdvdate(rdvDetails, idrdv));
			} else
				return ResponseEntity.badRequest().body(new ApiResponse(false, "RDV Not Found" + rdvDetails.getDay()));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/rdvs/medecin")
	Iterable<Rendezvous> rendezvousForMedecin() throws Exception {
		activityServices.createActivity(new Date(), "Read", "Show All Rdv for Medecin",
				globalVariables.getConnectedUser());
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		List<Rendezvous> l=rdvrepository.findByAllRdvByMedecin(medecin.getId());
		LOGGER.info("Show All Rdv for Medecin, UserID : " + globalVariables.getConnectedUser().getId());

		return l;
	}

	@GetMapping("/rdvs/medecins/cabinet")
	public ResponseEntity<?> getRdvForMedecinsByCabinet() {
		try {
			Long connectedUserId = globalVariables.getConnectedUser().getId();

			Medecin medecin = medRepo.getMedecinByUserId(connectedUserId);
			if (medecin == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Médecin non trouvé pour l'utilisateur connecté.");
			}

			Long cabinetId = medecin.getFirstCabinetId();
			if (cabinetId == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Aucun cabinet associé à ce médecin.");
			}

			List<Medecin> medecins = medRepo.getAllMedecinsByCabinetId(cabinetId);
			if (medecins == null || medecins.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun médecin trouvé pour ce cabinet.");
			}

			List<Rendezvous> allRendezvous = new ArrayList<>();
			for (Medecin med : medecins) {
				List<Rendezvous> medRdvs = rdvrepository.findByAllRdvByMedecin(med.getId());
				if (medRdvs != null && !medRdvs.isEmpty()) {
					allRendezvous.addAll(medRdvs);
				}
			}

			return ResponseEntity.ok(allRendezvous);

		} catch (Exception e) {
			// Log l'erreur (si tu as un logger type log.error())
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de la récupération des rendez-vous : " + e.getMessage());
		}
	}


//	@PostMapping(path = "/uploadDocRdv")
//	@ResponseBody
//	public ResponseEntity<?> uploadDocRdv(@RequestParam Long id, @RequestParam MultipartFile docFile)
//			throws Exception {
//		ObjectMapper om = new ObjectMapper();
//
//		String accountName = "documentspatient";
//		String accountKey = azureStorageToken;
//
//		StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
//
//		String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);
//
//		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential)
//				.buildClient();
//
//		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("documentspatient");
//
//		Rendezvous rendezvous = rdvrepository.getById(id);
//		Document doc = new Document();
//
//		doc.setDate_ajout_doc(new Date());
//		doc.setPatient(rendezvous.getPatient());
//		doc.setDossier(rendezvous.getPatient().getDossierMedical());
//
//		Document savedDoc = docrepository.save(doc);
//
//		String extension = FilenameUtils.getExtension(docFile.getOriginalFilename());
//
//		BlobClient blobClient = containerClient.getBlobClient(savedDoc.getId_doc() + "." + extension);
//
//		blobClient.upload(docFile.getInputStream(), docFile.getSize(), true);
//
//		savedDoc.setFichier_doc(blobClient.getBlobUrl());
//
//		docrepository.save(savedDoc);
//
//		// rendezvous.getDocuments().add(savedDoc);
//
//		rdvrepository.save(rendezvous);
//
//		activityServices.createActivity(new Date(), "Add", "Add New Document for rdv ID : " + id,
//				globalVariables.getConnectedUser());
//		LOGGER.info(
//				"Add New Document for rdv ID : " + id + " , UserID : " + globalVariables.getConnectedUser().getId());
//
//		return ResponseEntity.ok(new ApiResponse(true, "Document uploaded!"));
//
//	}

	// public static class RendezvousParams {
	//
	// public long id;
	// public Date jour;
	// public String motif;
	// public Medecin medecin;
	// public Patient patient;
	// }


	@GetMapping("/today/{spec}/{start}")
	RendezvousDTO rendezvousToday(@PathVariable("spec") long spec,@PathVariable("start") LocalDate start) throws Exception {

		RendezvousDTO rdv=rdvservice.getRdvToday(spec,start);
		return rdv;

	}

//	@GetMapping("/med/{iduser}")
//	public int getStatistics(@PathVariable("iduser") long iduser) throws Exception {
//		LOGGER.info("user : "+iduser);
//		Long idmed = medRepo.getMedecinByUserId(iduser).getId();
//		LOGGER.info("med : "+idmed);
//		LocalDate today = LocalDate.now();
//		return rdvservice.getStatisticsByMed(today, idmed);
//	}

	@GetMapping("/med")
	public ResponseEntity<?> getStatistics() {
		try {
			Long userId = globalVariables.getConnectedUser().getId();
			Long idmed = medRepo.getMedecinByUserId(userId).getId();
			LocalDate today = LocalDate.now();

			int dailyCount = rdvservice.getStatisticsByMed(today, idmed);
			int monthlyCount = rdvservice.getMonthlyStatisticsByMed(today.getYear(), today.getMonthValue(), idmed);
			int totalPatients = rdvservice.getTotalPatientsByMed(idmed);

			Map<String, Integer> statistics = new HashMap<>();
			statistics.put("day", dailyCount);
			statistics.put("month", monthlyCount);
			statistics.put("patients", totalPatients);

			return ResponseEntity.ok(statistics);

		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Utilisateur ou médecin introuvable."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Erreur interne du serveur."));
		}
	}

	@GetMapping("/rdvs/prochain")
	List<Rendezvous> prochainRendezvousForMedecin() throws Exception {

		Long idmed = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId()).getId();
		LOGGER.info("idmed : "+idmed);
		List<Rendezvous> rdvs=rdvservice.getRendezVousByMed(idmed);
//		activityServices.createActivity(new Date(), "Read", "Show Prochain Rdv for Medecin ",
//				globalVariables.getConnectedUser());
		LOGGER.info("Show Prochain Rdv for Medecin");
		return rdvs;

	}

	@GetMapping("/rdvs/bymonth/{month}/{year}")
	public ResponseEntity<?> getRendezvousByMonthAndYear(
			@PathVariable int month, @PathVariable int year) {
		try {
			Long userId = globalVariables.getConnectedUser().getId();
			Medecin medecin = medecinService.getMedecinByUserId(userId);
			List<Rendezvous> rdvs = rdvservice.getRendezVousByMedAndMonth(medecin.getId() , month , year);
			return ResponseEntity.ok(rdvs);
		} catch (NotFoundException e) {
            return  ResponseEntity.notFound().build();
        } catch (Exception e) {
             return ResponseEntity.status(404).body("Erreur lors de la récupération des rendez-vous : " + e.getMessage());
        }
    }

	////////////CHART
	/*@GetMapping("/count-by-mode")
	@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
	public ResponseEntity<List<ModeCountDTO>> countRendezvousByMode() {
		List<Object[]> results = rdvservice.getRendezvousCountByModeAndMonthYear();
		List<ModeCountDTO> dtos = new ArrayList<>();

		for (Object[] result : results) {
			ModeConsultationEnum mode = (ModeConsultationEnum) result[0];
			int year = (Integer) result[1];
			int month = (Integer) result[2];
			Long count = (Long) result[3];
			dtos.add(new ModeCountDTO(mode, year, month, count));
		}

		return ResponseEntity.ok(dtos);
	}*/

	/*@GetMapping("/count-by-mode")
	@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
	public ResponseEntity<List<ModeCountDTO>> countRendezvousByMode(
			@RequestParam(value = "year", required = false) Integer year,
			@RequestParam(value = "month", required = false) Integer month
	) {
		List<Object[]> results;
		if (year != null && month != null) {
			//to fetch data for actual month & year
			results = rdvservice.getRendezvousCountByModeAndMonthYear(year, month);
		} else {
			results = rdvservice.getRendezvousCountByModeAndMonthYear();
		}

		List<ModeCountDTO> dtos = new ArrayList<>();

		for (Object[] result : results) {
			ModeConsultationEnum mode = (ModeConsultationEnum) result[0];
			int resultYear = (Integer) result[1];
			int resultMonth = (Integer) result[2];
			Long count = (Long) result[3];
			dtos.add(new ModeCountDTO(mode, resultYear, resultMonth, count));
		}
		return ResponseEntity.ok(dtos);
	}*/

	/*@GetMapping("/count-by-mode")
	@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
	public ResponseEntity<List<ModeCountDTO>> countRendezvousByMode(
			@RequestParam(value = "year") int year,
			@RequestParam(value = "month") int month
	) {
		List<Object[]> results = rdvservice.getRendezvousCountByModeAndMonthYear(year, month);

		List<ModeCountDTO> dtos = new ArrayList<>();

		if (!results.isEmpty()) {
			for (Object[] result : results) {
				ModeConsultationEnum mode = (ModeConsultationEnum) result[0];
				Long count = 0L;

				if (result[3] != null) {
					count = (Long) result[3];
				}

				dtos.add(new ModeCountDTO(mode, resultYear, resultMonth, count));
			}
		} else {
			// Si aucun résultat, ajouter des valeurs par défaut pour chaque mode de consultation
			for (ModeConsultationEnum mode : ModeConsultationEnum.values()) {
				dtos.add(new ModeCountDTO(mode, year, month, 0L));
			}
		}

		return ResponseEntity.ok(dtos);
	}*/

	@GetMapping("/count-by-mode")
	@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
	public ResponseEntity<ModeCountDTO> countRendezvousByMode(@RequestParam int month, @RequestParam int year) {
		List<Object[]> results = rdvservice.getRendezvousCountByModeAndMonthYear(year, month);
		Long cabinetCount = 0L;
		Long videoCount = 0L;
		Long domicileCount = 0L;

		if (!results.isEmpty()) {
			Object[] counts = results.get(0);

			if (counts[0] != null) {
				cabinetCount = ((BigDecimal) counts[0]).longValue();
			}
			if (counts[1] != null) {
				videoCount = ((BigDecimal) counts[1]).longValue();
			}
			if (counts[2] != null) {
				domicileCount = ((BigDecimal) counts[2]).longValue();
			}
		}

		ModeCountDTO patientCounts = new ModeCountDTO(cabinetCount, videoCount, domicileCount);
		return ResponseEntity.ok(patientCounts);
	}

	@GetMapping("/patientCounts")
	@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
	public ResponseEntity<PatientCountsDTO> getPatientCounts(@RequestParam int month, @RequestParam int year) {
		List<Object[]> results = rdvservice.countsByCiviliteAndAge(month, year);
		Long femmeCount = 0L;
		Long hommeCount = 0L;
		Long enfantCount = 0L;

		if (!results.isEmpty()) {
			Object[] counts = results.get(0);

			if (counts[0] != null) {
				femmeCount = ((BigDecimal) counts[0]).longValue();
			}
			if (counts[1] != null) {
				hommeCount = ((BigDecimal) counts[1]).longValue();
			}
			if (counts[2] != null) {
				enfantCount = ((BigDecimal) counts[2]).longValue();
			}
		}

		PatientCountsDTO patientCounts = new PatientCountsDTO(femmeCount, hommeCount, enfantCount);
		return ResponseEntity.ok(patientCounts);
	}
	/*public ResponseEntity<PatientCountsDTO> getPatientCounts() {

		List<Object[]> results = rdvservice.countsByCiviliteAndAge();
		PatientCountsDTO patientCounts = null;

		if (!results.isEmpty()) {
			Object[] counts = results.get(0);
			Long femmeCount = ((BigDecimal) counts[0]).longValue();
			Long hommeCount = ((BigDecimal) counts[1]).longValue();
			Long enfantCount = ((BigDecimal) counts[2]).longValue();
			patientCounts = new PatientCountsDTO(femmeCount, hommeCount, enfantCount);
		}

		return ResponseEntity.ok(patientCounts);
	}*/
	/////////////



}

