package com.clinitalPlatform.controllers;

//import com.azure.storage.blob.BlobClient;
//import com.azure.storage.blob.BlobContainerClient;
//import com.azure.storage.blob.BlobServiceClient;
//import com.azure.storage.blob.BlobServiceClientBuilder;
//import com.azure.storage.common.StorageSharedKeyCredential;
import com.clinitalPlatform.dto.RendezvousDTO;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.FichePatientResponse;
import com.clinitalPlatform.payload.response.RendezvousResponse;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.services.ActivityServices;
//import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.services.PatientService;
import com.clinitalPlatform.services.RendezvousService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

//	@Autowired
//	MedecinServiceImpl medservice;

	@Autowired
	TypeConsultationRepository typeConsultRepo;

	@Autowired
	MedecinScheduleRepository medScheduleRepo;

	@Autowired
	PatientRepository patientRepo;

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

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Value(value = "${azure.storage.account-key}")
	String azureStorageToken;

	// Get all RDVs : a revoire ....
	@GetMapping("/rdvs")
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
	@GetMapping("patient/rdvById/{id}")
	public Rendezvous getRdvByIdBypatient(@PathVariable Long id) throws Exception {

		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Rendezvous IsRdv = rdvrepository.findById(id)
				.orElseThrow(() -> new BadRequestException("NO Matching Found for this Id Patient"));
		activityServices.createActivity(new Date(), "Read", "Consulting rdv for Patient by ID : " + id,
				globalVariables.getConnectedUser());
		LOGGER.info("Consulting Rdv for Patient by ID : " + id + ", UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvservice.findRdvByIdUserandId(globalVariables.getConnectedUser().getId(), IsRdv.getId());
	}

	// Get Rdv By Id and id medecin : %ok%
	@GetMapping("/med/rdvById/{id}")
	public Rendezvous getRdvByIdBymedecin(@PathVariable Long id) throws Exception {

		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		Rendezvous IsRdv = rdvrepository.findById(id)
				.orElseThrow(() -> new BadRequestException("NO Matching Found for this Id "));
		activityServices.createActivity(new Date(), "Read", "Consulting rdv for Medecin by ID : " + id,
				globalVariables.getConnectedUser());
		LOGGER.info("Consulting Rdv for Medecin by ID : " + id + ", UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvrepository.findRdvByIdandMedecin(IsRdv.getId(), medecin.getId())
				.orElseThrow(() -> new BadRequestException("NO Matching Found"));

	}

	// Get Rdv By ID patient for Medecin :
	@GetMapping("/med/rdvByIdPatient/{id}")
	@ResponseBody
	public List<RendezvousResponse> findRdvByIdPatient(@PathVariable Long id) throws BadRequestException, Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Patient patient = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId(), id)
				.orElseThrow(() -> new BadRequestException("NO Matching Found for this Id "));
		activityServices.createActivity(new Date(), "Read", "Consulting rdv for Patient by his ID : " + id,
				globalVariables.getConnectedUser());
		LOGGER.info("Consulting Rdv for Patient by his ID : " + id + ", UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvrepository.getRdvByIdPatient(patient.getId()).stream()
				.map(rdv -> mapper.map(rdv, RendezvousResponse.class)).collect(Collectors.toList());

	}

	// Get Rdv By ID Medecin for Patient :
	@GetMapping("/patient/rdvByIdMedecin")
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

	// Get RDV By patient Name : %OK%
	@GetMapping("med/rdvByNomPatient/{nompat}")
	@ResponseBody
	public List<Rendezvous> getRdvByNomPatientandMedecin(
			@PathVariable(value = "nompat") @NotNull String nomPatient) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		activityServices.createActivity(new Date(), "Read",
				"Consulting rdv for Medecin by Patient Name : " + nomPatient, globalVariables.getConnectedUser());
		LOGGER.info("Consulting Rdv for Medecin by Patient Name : " + nomPatient + ", UserID : "
				+ globalVariables.getConnectedUser().getId());
		return rdvrepository.getRdvByNomPatientByMedecin(nomPatient, medecin.getId()).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class)).collect(Collectors.toList());
	}

	// Get RDV By patient Name : %OK% getRdvByNomPatientByMedecin
	@GetMapping("patient/rdvByNomPatient/{nompat}")
	@ResponseBody
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
	@PostMapping("/med/addRdv")
	@ResponseBody
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public ResponseEntity<?> addRendezvous(@Valid @RequestBody RendezvousDTO c) throws Exception {

		try {
			// UserDetailsImpl userDetails = (UserDetailsImpl)
			// SecurityContextHolder.getContext().getAuthentication()
			// .getPrincipal();
			Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
			Patient patient = patientRepo.findById(c.getPatientid())
					.orElseThrow(() -> new Exception("NO Matching Found for this patient"));
			return ResponseEntity.ok(rdvservice.AddnewRdv(globalVariables.getConnectedUser(), c, medecin, patient));

		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}

	}

	// ADD an RDV by Patient : %OK%
	@PostMapping("/patient/addRdv")
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

	// DELETE AN RDV By Medecin : %ok%
//	@DeleteMapping("/med/delete/{id}")
//	public ResponseEntity<?> deleteRdvbyMedecin(@Valid @PathVariable Long id) throws Exception {
//		// UserDetailsImpl userDetails = (UserDetailsImpl)
//		// SecurityContextHolder.getContext().getAuthentication()
//		// .getPrincipal();
//		Medecin medecin = medservice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
//		Optional<Rendezvous> rdv = rdvrepository.findRdvByIdandMedecin(id, medecin.getId());
//
//		if (rdv.isPresent()) {
//
//			rdvservice.deleteRendezvous(id);
//			activityServices.createActivity(new Date(), "Delete", "Medecin Delete Rdv By : " + id,
//					globalVariables.getConnectedUser());
//			LOGGER.info(
//					"Medecin Delete  Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
//			return ResponseEntity.ok(new ApiResponse(true, "RDV has been deleted Seccussefully"));
//		} else
//			activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
//					globalVariables.getConnectedUser());
//		LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
//		return ResponseEntity.ok(
//				new ApiResponse(false, "Rendez-vous non trouver pour id :: " + id + " med id " + medecin.getId()));
//
//	}

	// DELETE AN RDV By Patient : %ok%
	@DeleteMapping("/patient/delete/{id}")
	public ResponseEntity<?> deleteRdvbyPatient(@Valid @PathVariable Long id) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Patient pat = patientService.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());

		Optional<Rendezvous> rdv = rdvrepository.findRdvByIdandPatient(id, pat.getId());
		if (rdv.isPresent()) {

			rdvservice.deleteRendezvous(id);
			activityServices.createActivity(new Date(), "Delete", "Patient Delete Rdv By : " + id,
					globalVariables.getConnectedUser());
			LOGGER.info(
					"Patient Delete  Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(new ApiResponse(true, "RDV has been deleted Seccussefully"));
		} else
			activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
					globalVariables.getConnectedUser());
		LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
		return ResponseEntity.ok(new ApiResponse(false, "Rendez-vous non trouver pour id :: " + id));

	}

	// Update RDV by Patient : %OK%
	@PostMapping("/patient/updateRdv/{id}")
	@ResponseBody
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public ResponseEntity<?> updateRdvByPat(@Valid @RequestBody FichePatientResponse.RendezvousRequest rdvDetails,
			@Valid @PathVariable(value = "id") long idrdv)
			throws BadRequestException {
		try {
			Optional<Rendezvous> isRdv = rdvrepository.getRendezvousById(idrdv);
			if (isRdv.isPresent()) {

				Patient patient = patientRepo.findById(rdvDetails.getPatientid()).orElseThrow(
						() -> new BadRequestException("Patient not found for this id :: " +
								rdvDetails.getPatientid()));
				Medecin med = medRepo.findById(rdvDetails.getMedecinid()).orElseThrow(
						() -> new BadRequestException("Patient not found for this id :: " +
								rdvDetails.getMedecinid()));
				// UserDetailsImpl userDetails = (UserDetailsImpl)
				// SecurityContextHolder.getContext().getAuthentication()
				// .getPrincipal();
				rdvservice.UpdateRdvByIdPatient(rdvDetails, idrdv);
				activityServices.createActivity(new Date(), "Update", "Patient Update Rdv By : " + idrdv,
						globalVariables.getConnectedUser());
				LOGGER.info("Patient Update  Rdv By ID : " + idrdv + ", UserID : "
						+ globalVariables.getConnectedUser().getId());
				return ResponseEntity.ok(new ApiResponse(true, "update Done"));

			} else
				activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + idrdv,
						globalVariables.getConnectedUser());
			LOGGER.warn(
					"Cannot found Rdv By ID : " + idrdv + ", UserID : " + globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found" + rdvDetails.getDay()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(null);

		}
	}

	// Update RDv bay Medecin : %ok%
	@PostMapping("/med/updateRdv/{id}")
	@ResponseBody
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public ResponseEntity<?> updateRdvByMed(@Valid @RequestBody FichePatientResponse.RendezvousRequest rdvDetails,
			@Valid @PathVariable(value = "id") long idrdv)
			throws BadRequestException {
		try {
			Optional<Rendezvous> isRdv = rdvrepository.getRendezvousById(idrdv);
			if (isRdv.isPresent()) {

				Patient patient = patientRepo.findById(rdvDetails.getPatientid()).orElseThrow(
						() -> new BadRequestException("Patient not found for this id :: " +
								rdvDetails.getPatientid()));
				// UserDetailsImpl userDetails = (UserDetailsImpl)
				// SecurityContextHolder.getContext().getAuthentication()
				// .getPrincipal();

				Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
				rdvservice.UpdateRdvByIdMedecin(rdvDetails, idrdv, medecin.getId());
				activityServices.createActivity(new Date(), "Update", "Medecin Update Rdv By : " + idrdv,
						globalVariables.getConnectedUser());
				LOGGER.info("Medecin Update  Rdv By ID : " + idrdv + ", UserID : "
						+ globalVariables.getConnectedUser().getId());
				return ResponseEntity.ok(new ApiResponse(true, "Update Seccuessfully"));

			} else
				activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + idrdv,
						globalVariables.getConnectedUser());
			LOGGER.warn(
					"Cannot found Rdv By ID : " + idrdv + ", UserID : " + globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found" + rdvDetails.getDay()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(null);

		}
	}

	// Get Rdv For connected Medecin : %OK%
	@GetMapping("/rdvs/medecin")
	Iterable<RendezvousResponse> rendezvousForMedecin() throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		activityServices.createActivity(new Date(), "Read", "Show All Rdv for Medecin",
				globalVariables.getConnectedUser());
		LOGGER.info("Show All Rdv for Medecin, UserID : " + globalVariables.getConnectedUser().getId());
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		return rdvrepository.findByAllRdvByMedecin(medecin.getId()).stream()
				.map(rdv -> mapper.map(rdv, RendezvousResponse.class))
				.collect(Collectors.toList());
	}

	// Get Rdv For connected Patient : %OK%
	@GetMapping("/rdvs/patient")
	List<Rendezvous> rendezvousForPatient() throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());
		List<Rendezvous> rdvpatient = null;
		for (Patient pat : patients) {

			rdvpatient = rdvrepository.findAllRdvByPatient(pat.getId()).stream()
					.map(rdv -> mapper.map(rdv, Rendezvous.class))
					.collect(Collectors.toList());
		}
		activityServices.createActivity(new Date(), "Read", "Show All Rdv for Patients",
				globalVariables.getConnectedUser());
		LOGGER.info("Show All Rdv for Patients, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvpatient;

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
//	@PostMapping("/med/cancelRdv/{id}")
//	public ResponseEntity<?> cancelRdvByMedecin(@Valid @PathVariable Long id) throws Exception {
//		// Rendezvous rdv = rdvrepository.findById(id)
//		// .orElseThrow(() -> new BadRequestException("Rendez-vous not found for this id
//		// :: " + id));
//		// UserDetailsImpl userDetails = (UserDetailsImpl)
//		// SecurityContextHolder.getContext().getAuthentication()
//		// .getPrincipal();
//		Medecin med = medservice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
//
//		Optional<Rendezvous> isRdv = rdvrepository.findRdvByIdandMedecin(id, med.getId());
//		if (isRdv.isPresent()) {
//
//			Rendezvous rdv = rdvrepository.findRdvByIdandMedecin(id, med.getId()).get();
//
//			rdv.setCanceledAt(LocalDateTime.now());
//			rdv.setStatut(RdvStatutEnum.ANNULE);
//			final Rendezvous updatedrdv = rdvrepository.save(rdv);
//			activityServices.createActivity(new Date(), "Update", "Medecin Cancel Rdv ID : " + id,
//					globalVariables.getConnectedUser());
//			LOGGER.info("Medecin Cancel Rdv ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
//			return ResponseEntity.ok(mapper.map(updatedrdv, Rendezvous.class));
//		} else
//			activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
//					globalVariables.getConnectedUser());
//		LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
//		return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found" + id));
//
//	}

	// cancel Rdv For connected Patient : %OK%
	@PostMapping("/patient/cancelRdv/{id}")
	public ResponseEntity<?> cancelRdvByPatient(@Valid @PathVariable Long id) throws Exception {
		// Rendezvous rdv = rdvrepository.findById(id)
		// .orElseThrow(() -> new BadRequestException("Rendez-vous not found for this id
		// :: " + id));
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Patient pat = patientService.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());

		Optional<Rendezvous> isRdv = rdvrepository.findRdvByIdandPatient(id, pat.getId());
		if (isRdv.isPresent()) {

			Rendezvous rdv = rdvrepository.findRdvByIdandPatient(id, pat.getId()).get();

			rdv.setCanceledAt(LocalDateTime.now());
			rdv.setStatut(RdvStatutEnum.ANNULE);
			final Rendezvous updatedrdv = rdvrepository.save(rdv);
			activityServices.createActivity(new Date(), "Update", "Patient Cancel Rdv ID : " + id,
					globalVariables.getConnectedUser());
			LOGGER.info("Patient Cancel Rdv ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
			return ResponseEntity.ok(mapper.map(updatedrdv, RendezvousResponse.class));
		} else
			activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
					globalVariables.getConnectedUser());
		LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
		return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found" + id));

	}

	// CHANGE RDV Status for connected Medecin : %OK% les autres status.
//	@PostMapping("/med/changestatu/{id}")
//	public ResponseEntity<?> ChangeRdvSttByMedecin(@Valid @PathVariable Long id,
//			@Valid @RequestBody FichePatientResponse.RendezvousRequest requestrdv) throws Exception {
//		// Rendezvous rdv = rdvrepository.findById(id)
//		// .orElseThrow(() -> new BadRequestException("Rendez-vous not found for this id
//		// :: " + id));
//		// UserDetailsImpl userDetails = (UserDetailsImpl)
//		// SecurityContextHolder.getContext().getAuthentication()
//		// .getPrincipal();
//		Medecin med = medservice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
//
//		Optional<Rendezvous> isRdv = rdvrepository.findRdvByIdandMedecin(id, med.getId());
//		if (isRdv.isPresent()) {
//
//			Rendezvous rdv = rdvrepository.findRdvByIdandMedecin(id, med.getId()).get();
//			rdv.setStatut(requestrdv.getStatut());
//			final Rendezvous updatedrdv = rdvrepository.save(rdv);
//			activityServices.createActivity(new Date(), "Update",
//					"Medecin Change Rdv Statue ID : " + id + " to " + requestrdv.getStatut(),
//					globalVariables.getConnectedUser());
//			LOGGER.info("Medecin Change Rdv Statue ID : " + id + " to " + requestrdv.getStatut() + ", UserID : "
//					+ globalVariables.getConnectedUser().getId());
//			return ResponseEntity.ok(mapper.map(updatedrdv, Rendezvous.class));
//		} else
//			activityServices.createActivity(new Date(), "Warning", "Cannot found Rdv By ID : " + id,
//					globalVariables.getConnectedUser());
//		LOGGER.warn("Cannot found Rdv By ID : " + id + ", UserID : " + globalVariables.getConnectedUser().getId());
//		return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found" + id));
//
//	}

	// CHANGE RDV Status for connected Medecin : %OK% les autres status.
	@PostMapping("/patient/changestatu/{id}")
	public ResponseEntity<?> ChangeRdvSttByPatient(@Valid @PathVariable Long id,
			@Valid @RequestBody FichePatientResponse.RendezvousRequest requestrdv) throws Exception {
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
	// RDV FOR DOCTOR BY DAY :
	@GetMapping("/med/rdvbyday/{day}")
	List<Rendezvous> rendezvousMedByday(@Valid @PathVariable long day) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Medecin By Day : " + day,
				globalVariables.getConnectedUser());
		LOGGER.info("Medecin Consult All Rdv by Day, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByDay(day, medecin.getId()).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	// RDV FOR DOCTOR BY WEEK :
	@GetMapping("/med/rdvbyweek/{week}")
	List<Rendezvous> rendezvousMedByweek(@Valid @PathVariable long week) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Medecin By Week : " + week,
				globalVariables.getConnectedUser());
		LOGGER.info("Medecin Consult All Rdv by Week, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByWeek(week, medecin.getId()).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	// RDV FOR DOCTOR BY MONTH :
	@GetMapping("/med/rdvbymonth/{month}")
	List<Rendezvous> rendezvousMedBymonth(@Valid @PathVariable long month) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Medecin By Month : " + month,
				globalVariables.getConnectedUser());
		LOGGER.info("Medecin Consult All Rdv by Month, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByMonth(month, medecin.getId()).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	// RDV FOR DOCTOR BY YEAR :
	@GetMapping("/med/rdvbyyear/{year}")
	List<Rendezvous> rendezvousMedByyear(@Valid @PathVariable long year) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		Medecin medecin = medRepo.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Medecin By Year : " + year,
				globalVariables.getConnectedUser());
		LOGGER.info("Medecin Consult All Rdv by Year, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvservice.getRdvMedByYear(year, medecin.getId()).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class))
				.collect(Collectors.toList());
	}

	/*
	 * Patient : RDV FOR Patient BY DAY :
	 */
	@GetMapping("/patient/rdvbyday/{day}")
	List<Rendezvous> rendezvousPatientByday(@Valid @PathVariable long day) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());

		List<Rendezvous> rdvpatient = null;
		for (Patient pat : patients) {

			rdvpatient = rdvservice.getRdvMedByDay(day, pat.getId()).stream()
					.map(rdv -> mapper.map(rdv, Rendezvous.class))
					.collect(Collectors.toList());
		}
		activityServices.createActivity(new Date(), "Read", "Consult Rdv for Patient By Month : " + day,
				globalVariables.getConnectedUser());
		LOGGER.info("Consult All Rdv for My patients by day, UserID : " + globalVariables.getConnectedUser().getId());
		return rdvpatient;
	}

	// RDV FOR Patient BY WEEK :
	@GetMapping("/patient/rdvbyweek/{week}")
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
	List<Rendezvous> rendezvousPatientBymonth(@Valid @PathVariable long month) throws Exception {
		// UserDetailsImpl userDetails = (UserDetailsImpl)
		// SecurityContextHolder.getContext().getAuthentication()
		// .getPrincipal();
		List<Patient> patients = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId());

		List<Rendezvous> rdvpatient = null;
		for (Patient pat : patients) {

			rdvpatient = rdvservice.getRdvMedByMonth(month, pat.getId()).stream()
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
	public ResponseEntity<?> MoveRDV(@Valid @RequestBody FichePatientResponse.RendezvousRequest rdvDetails,
			@Valid @PathVariable(value = "id") long idrdv)
			throws BadRequestException {
		try {
			Optional<Rendezvous> isRdv = rdvrepository.getRendezvousById(idrdv);
			if (isRdv.isPresent()) {
				// update
				return ResponseEntity.ok(rdvservice.UpdateRdvdate(rdvDetails, idrdv));

			} else

				return ResponseEntity.ok(new ApiResponse(false, "RDV Not Found" + rdvDetails.getDay()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(null);

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

}
