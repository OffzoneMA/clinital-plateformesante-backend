package com.clinitalPlatform.controllers;


import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.services.PatientService;
import com.clinitalPlatform.services.RendezvousService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patient")
@Slf4j
public class PatientController {

	@Autowired
	PatientRepository patientRepo;

	@Autowired
	PatientService patientService;

	@Autowired
	ClinitalModelMapper mapper;

	@Autowired
	UserRepository userRepo;

	@Autowired
	VilleRepository villeRepository;

	@Autowired
	RendezvousService rendezvousService;

	@Autowired
	MedecinScheduleRepository medScheduleRepo;

	@Autowired
	TypeConsultationRepository typeConsultationRepo;

	@Autowired
	private MedecinServiceImpl medecinService;

	//@Autowired
	//private ConsultationServices consultationServices;

	@Autowired
	GlobalVariables globalVariables;

	@Autowired
	private ActivityServices activityServices;

	private final Logger LOGGER=LoggerFactory.getLogger(getClass());


	// Get patient by ID :
//	@GetMapping("/getPatientById/{account}")
//	@ResponseBody
//	public PatientResponse findPatientByAccount(@PathVariable("account") Long userID) throws Exception {
//
//		activityServices.createActivity(new Date(),"Read","Consulting Info of Patient ID : "+userID,globalVariables.getConnectedUser());
//		LOGGER.info("Consulting Info of Patient ID : "+userID+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//		return mapper.map(patientRepo.findPatientByAccount(userID), PatientResponse.class);
//	}
//
//	@GetMapping("/getall/type/{type}")
//	@ResponseBody
//	public List<PatientResponse> findPatientByType(@PathVariable PatientTypeEnum type) throws Exception {
//		activityServices.createActivity(new Date(),"Read","Consulting All Patient by type : "+type,globalVariables.getConnectedUser());
//		LOGGER.info("Consulting All Patient by type : "+type+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//
//		return patientRepo.findPatientByType(type).stream().map(pat -> mapper.map(pat, PatientResponse.class))
//				.collect(Collectors.toList());
//	}
//
//	// A method that is used to add a patient to the database. %OK%
//	@PostMapping("/addpatient")
//	@ResponseBody
//	public ResponseEntity<?> addPatient(@Valid @RequestBody PatientRequest patient) throws Exception {
//		if (patient != null) {
//			// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//			// 		.getPrincipal();
//					Patient patientEntity=new Patient();
//					patientEntity= mapper.map(patient, Patient.class);
//
//					Ville ville = villeRepository.findById(patient.getVilleId())
//					.orElseThrow(
//							() -> new BadRequestException("Ville not found for this id :: " + patient.getVilleId()));
//
//			patientEntity.setUser(userRepo.getById(globalVariables.getConnectedUser().getId()));
//			// patientEntity.setVille(ville);
//			if (patientEntity.getPatient_type() == null) {
//				patientEntity.setPatient_type(PatientTypeEnum.MOI);
//
//			} else {
//				patientEntity.setPatient_type(PatientTypeEnum.PROCHE);
//			}
//			patientEntity.setPatientEmail(patient.getPatientEmail());
//			Patient pat = patientService.create(patientEntity);
//			activityServices.createActivity(new Date(),"Add","Add New Patient ID : "+pat.getId(),globalVariables.getConnectedUser());
//		LOGGER.info("Add New Patient ID : "+pat.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//			return ResponseEntity.ok(mapper.map(pat, Patient.class));
//
//		} else
//			return ResponseEntity.ok(new ApiResponse(false, "no DATA"));
//	}
//
//	// Deleting a patient by id. %OK%
//	@DeleteMapping("/delete/{id}")
//	public Map<String, Boolean> deletePatient(@PathVariable Long id) throws Exception {
//		UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//				.getPrincipal();
//
//		Map<String, Boolean> response = new HashMap<>();
//		Optional<Patient> patient = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId(), id);
//		if (patient.isPresent()) {
//			Patient pt = patientRepo.getById(id);
//			if (pt.getPatient_type() == PatientTypeEnum.PROCHE) {
//				patientService.delete(pt);
//				activityServices.createActivity(new Date(),"Delete","Delete Patient ID : "+pt.getId(),globalVariables.getConnectedUser());
//				LOGGER.info("Delete Patient by ID : "+pt.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//				response.put("deleted", Boolean.TRUE);
//			} else {
//				activityServices.createActivity(new Date(),"Add","Cannot Delete Patient ID : "+pt.getId(),globalVariables.getConnectedUser());
//				LOGGER.error("Cannot Patient ID : "+pt.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//				response.put("NOt authorized to delete Your self now", Boolean.FALSE);
//			}
//
//		} else {
//			response.put("nomatch", Boolean.FALSE);
//		}
//		return response;
//	}
//
//	// Updating a patient by id. %OK%
//	@PostMapping("/updatepatient/{id}")
//	@ResponseBody
//	public ResponseEntity<?> updatePatient(@Valid @PathVariable Long id,
//			@Valid @RequestBody PatientRequest patient) throws Exception {
//
//		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//		// 		.getPrincipal();
//
//		Optional<Patient> pat = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId(), id);
//		if (pat.isPresent()) {
//
//			try {
//				Patient pt = patientRepo.getById(id);
//				Ville ville = villeRepository.findById(patient.getVilleId())
//						.orElseThrow(() -> new BadRequestException(
//								"Ville not found for this id :: " + patient.getVilleId()));
//
//				pt.setNom_pat(patient.getNom_pat());
//				pt.setPrenom_pat(patient.getPrenom_pat());
//				pt.setDateNaissance(patient.getDateNaissance());
//				pt.setAdresse_pat(patient.getAdresse_pat());
//				pt.setCodePost_pat(patient.getCodePost_pat());
//				pt.setMatricule_pat(patient.getMatricule_pat());
//				pt.setCivilite_pat(patient.getCivilite_pat());
//				pt.setPatientEmail(patient.getPatientEmail());
//				pt.setPatientTelephone(patient.getPatientTelephone());
//				pt.setPlaceOfBirth(patient.getPlaceOfBirth());
//				pt.setMutuelNumber(patient.getMutuelNumber());;
//				pt.setVille(ville);
//				// pt.setUser(mapper.map(userRepo.getOne(globalVariables.getConnectedUser().getId()), User.class));
//				pt.setPatient_type(patient.getPatient_type());
//
//				Patient updatedpt = patientRepo.save(pt);
//				activityServices.createActivity(new Date(),"Update","Update Patient ID : "+pt.getId(),globalVariables.getConnectedUser());
//				LOGGER.info("Update Patient ID : "+pt.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//				return ResponseEntity.ok(mapper.map(updatedpt, Patient.class));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			return ResponseEntity.ok(new ApiResponse(false, "Pas de Patient avec ce id :: " + id));
//		}
//		return null;
//
//	}
//
//	/**
//	 * It returns a Patient object from the database, where the user_id is equal to
//	 * the id parameter, and
//	 * the patient_type is equal to the string "MOI" it mean the current USER
//	 * connected
//	 *
//	 * @param id the id of the user
//	 * @return Patient
//	 * @throws Exception
//	 */
//	@GetMapping("/getmypatientaccount") // %OK%
//	@ResponseBody
//	public ResponseEntity<PatientResponse> getPatientByUserId() throws Exception {
//		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//		// 		.getPrincipal();
//
//		Patient patient = patientService.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());
//		System.out.println(patient.getId());
//		mapper.typeMap(Patient.class, PatientResponse.class)
//		.addMapping(src -> src.getVille().getId_ville(), PatientResponse::setVilleId);
//
//  		PatientResponse patientResponse = mapper.map(patient, PatientResponse.class);
//			activityServices.createActivity(new Date(),"Read","Consulting personal Patient Account : "+patient.getId(),globalVariables.getConnectedUser());
//			LOGGER.info("Consulting Personal Patient Account ID : "+patient.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//  		return ResponseEntity.ok(patientResponse);
//
//
//
//	}
//
//	/**
//	 * I want to find a patient by his id and his user_id and his patient_type
//	 *
//	 * @param id        the id of the user
//	 * @param idpatient the id of the patient
//	 * @return A list of patients
//	 * @throws Exception
//	 */
//	@GetMapping("/findpatientproch/{idpat}") // %OK%
//	@ResponseBody
//	public ResponseEntity<?> findProchByUserId(@Valid @PathVariable long idpat) throws Exception {
//		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//		// 		.getPrincipal();
//
//		Patient patientUser=patientRepo.findPatientByAccount(globalVariables.getConnectedUser().getId());
//		Boolean patient = patientRepo.existsById(idpat);
//		if (patient && idpat!=patientUser.getId()) {
//			activityServices.createActivity(new Date(),"Read","Cosnulting Proch Patient ID : "+idpat,globalVariables.getConnectedUser());
//			LOGGER.info("Consulting Proch Patient ID  : "+idpat+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//			return ResponseEntity.ok(patientService.findProchByUserId(globalVariables.getConnectedUser().getId(), idpat));
//		} else
//		activityServices.createActivity(new Date(),"Read","Cant consult this Account : "+idpat,globalVariables.getConnectedUser());
//		LOGGER.warn("Cant Consult this Account ID : "+idpat+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//			return ResponseEntity.ok(new ApiResponse(false, "Pas de Patient avec ce id :: " + idpat));
//
//	}

	/**
	 * It returns a list of patients that are related to the user with the id passed
	 * as a parameter
	 * where type is PROCHE
	 *
	 * @return List of Patient %OK%
	 * @throws Exception
	 */
	@GetMapping("/getallproch")
	@ResponseBody
	public ResponseEntity<?> findALLProchByUserId() throws Exception {

		
		activityServices.createActivity(new Date(),"Read","Consulting All Proch Account",globalVariables.getConnectedUser());
		LOGGER.info("Consulting All Proch Account, UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return ResponseEntity.ok(patientService.findALLProchByUserId(globalVariables.getConnectedUser().getId()));

	}

	/**
	 * It returns a list of patients that have the same user_id as the id passed in
	 *
	 * @return List of Patient objects %OK%
	 * @throws Exception
	 */
	@GetMapping("/getall")
	//@ResponseBody
	public List<Patient> findALLPatientByUserId() throws Exception {

		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
		// 		.getPrincipal();
		activityServices.createActivity(new Date(),"Read","Consulting All Patient Account",globalVariables.getConnectedUser());
		LOGGER.info("Consulting All Patient Account, UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return patientService.findALLPatientByUserId(globalVariables.getConnectedUser().getId());

	}

	// end point for Agenda of a patient : %OK%
//	@PostMapping("/agendapatient/{startDate}")
//	@JsonSerialize(using = LocalDateSerializer.class)
//	public List<AgendaResponse> findRDVschedules(@Valid @PathVariable String startDate) {
//		List<AgendaResponse> agendaResponseList = new ArrayList<AgendaResponse>();
//		try {
//
//			// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//			// 		.getPrincipal();
//			List<Patient> patient = patientService.findALLPatientByUserId(globalVariables.getConnectedUser().getId());
//
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//			LocalDateTime startdateTime = LocalDateTime.parse(startDate, formatter);
//
//			for (int i = 1; i <= 7; i++) {
//				AgendaResponse agendaResponse = new AgendaResponse();
//
//				agendaResponse.setDay(DayOfWeek.of(i));
//
//				LocalDateTime nowDate = LocalDateTime.now();
//				if (startDate != null)
//					nowDate = startdateTime;
//
//				if (nowDate.getDayOfWeek().getValue() <= i) {
//					log.info("Found Day Greater than");
//
//					for (Patient pat : patient) {
//
//						List<Rendezvous> rendezvous = rendezvousService.getRdvPatientByDayWeek(i, pat.getId());
//
//						if (!rendezvous.isEmpty()) {
//							for (Rendezvous rd : rendezvous) {
//
//								agendaResponse.getAvailableSlot()
//										.add((rd.getStart().getHour() < 10 ? "0" : "") + rd.getStart().getHour() + ":"
//												+ (rd.getStart().getMinute() < 10 ? "0" : "")
//												+ rd.getStart().getMinute());
//							}
//
//						}
//
//					}
//
//				} else {
//					log.info("Later Days");
//				}
//
//				agendaResponseList.add(agendaResponse);
//			}
//		} catch (Exception e) {
//			log.info("Warning mapping issue", e.getMessage());
//		}
//
//		return agendaResponseList;
//
//	}
//
//	// share folder with a doctor :
//
//	@PostMapping("/sharedoc/{idmed}/{iddoss}")
//	public ResponseEntity<?> ShareAccesstoFolder(@Valid @PathVariable Long idmed,@Valid @PathVariable("iddoss") Long iddossier) throws Exception{
//		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//        //         .getPrincipal();
//		Patient patient=patientRepo.findPatientByUserIdandDossMedicale(globalVariables.getConnectedUser().getId(),iddossier).orElseThrow(()->new Exception("NO MATCHING FOUND FOR THAT USER !"));
//
//		activityServices.createActivity(new Date(),"Update","Sharing Medical Folder ID : "+iddossier+" with Medecin : "+idmed,globalVariables.getConnectedUser());
//		LOGGER.info("Sharing Medical Folder ID : "+iddossier+" with Medecin : "+idmed+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//		return ResponseEntity.ok(patientService.ShareMedecialFolder(patient.getDossierMedical().getId_dossier(),idmed));
//
//	}
//
//// generate fiche patient  :
//
//	@GetMapping("/getfichepatient/{idpat}")
//	public FichePatientResponse gFichePatient(@PathVariable Long idpat ) throws Exception{
//
//			// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//			// 		.getPrincipal();
//			FichePatientResponse fiche = patientService.Fichepatient(idpat, globalVariables.getConnectedUser().getId());
//			activityServices.createActivity(new Date(),"Read","Consulting Fiche Patiend ID : "+idpat,globalVariables.getConnectedUser());
//		LOGGER.info("Consulting Fiche Patient ID :"+idpat+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//			return fiche;
//
//	}
//
////--------------------------------------------------------------------------------------------------
//// show data by PATIENT access right
//
//@GetMapping(path = "/findconsult/{idpat}/{idconsult}")
//public ResponseEntity<?> findByIdMedecin(@Valid @PathVariable Long idpat,@Valid @PathVariable(value = "idconsult")Long idcons){
//
//	try {
//
//		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//		// 			.getPrincipal();
//		Patient patient= patientService.findALLPatientByUserId(globalVariables.getConnectedUser().getId()).stream().filter(pat->(pat.getId()==idpat)).findFirst().orElseThrow(()->new Exception("No Matching Found"));
//
//		ConsultationDTO consul=consultationServices.findByIdPatient(idcons, patient.getId());
//
//		activityServices.createActivity(new Date(),"Read","Consulting Cosnultation ID : "+idcons+"for Patiend ID : "+idpat,globalVariables.getConnectedUser());
//		LOGGER.info("Consulting Cosnultation ID : "+idcons+"for Patiend ID : "+idpat+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//
//		return ResponseEntity.ok(consul);
//	} catch (Exception e) {
//		e.printStackTrace();
//		return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
//	}
//
//
//
//}
//@GetMapping(path = "/allconsult/{idpat}")
//public ResponseEntity<?> findAllByIdPatient(@Valid @PathVariable Long idpat){
//
//	try {
//
//		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//		// 			.getPrincipal();
//		Patient patient= patientService.findALLPatientByUserId(globalVariables.getConnectedUser().getId()).stream().filter(pat->(pat.getId()==idpat)).findFirst().orElseThrow(()->new Exception("No Matching Found"));
//		List<ConsultationDTO> allconsul=consultationServices.findALLByIdPatient(patient.getId());
//		activityServices.createActivity(new Date(),"Read","Consulting All Cosnultation for Patiend ID : "+idpat,globalVariables.getConnectedUser());
//		LOGGER.info("Consulting All Cosnultation for Patiend ID : "+idpat+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//		return ResponseEntity.ok(allconsul);
//	} catch (Exception e) {
//		e.printStackTrace();
//		return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
//
//	}
//

//}
 

}
