package com.clinitalPlatform.controllers;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.enums.PatientTypeEnum;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.models.Ville;
import com.clinitalPlatform.payload.request.PatientRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.PatientResponse;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.PatientService;
import com.clinitalPlatform.services.RendezvousService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.util.*;

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
   GlobalVariables globalVariables;

	@Autowired
	private ActivityServices  activityServices;

	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
  

	// Get patient by ID :
	@GetMapping("/getPatientById/{account}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	@ResponseBody
	public PatientResponse findPatientByAccount(@PathVariable("account") Long userID) throws Exception {

		activityServices.createActivity(new Date(),"Read","Consulting Info of Patient ID : "+userID,globalVariables.getConnectedUser());
		LOGGER.info("Consulting Info of Patient ID : "+userID+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return mapper.map(patientRepo.findPatientByAccount(userID), PatientResponse.class);
	}
	
	// A method that is used to add a patient to the database. %OK%
	@PostMapping("/addpatient")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	@ResponseBody
	public ResponseEntity<?> addPatient(@Valid @RequestBody PatientRequest patient) throws Exception {
		if (patient != null) {
			
					Patient patientEntity=new Patient();
					patientEntity= mapper.map(patient, Patient.class);
					Ville ville = villeRepository.findById(patient.getVilleId())
							.orElseThrow(
									() -> new BadRequestException("Ville not found for this id :: " + patient.getVilleId()));


			patientEntity.setUser(userRepo.getById(globalVariables.getConnectedUser().getId()));
			patientEntity.setVille(ville);
			if (patientEntity.getPatient_type() == null) {
				
				patientEntity.setPatient_type(PatientTypeEnum.MOI);
				
			} else {
				patientEntity.setPatient_type(PatientTypeEnum.PROCHE);
			}
			patientEntity.setPatientEmail(patient.getPatientEmail());
			Patient pat = patientService.create(patientEntity);
			activityServices.createActivity(new Date(),"Add","Add New Patient ID : "+pat.getId(),globalVariables.getConnectedUser());
		LOGGER.info("Add New Patient ID : "+pat.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
			return ResponseEntity.ok(mapper.map(pat, Patient.class));

		} else
			return ResponseEntity.ok(new ApiResponse(false, "no DATA"));
	}

	// Deleting a patient by id. %OK%
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	public Map<String, Boolean> deletePatient(@PathVariable Long id) throws Exception {
		
		Map<String, Boolean> response = new HashMap<>();
		Optional<Patient> patient = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId(), id);
		if (patient.isPresent()) {
			Patient pt = patientRepo.getById(id);
			if (pt.getPatient_type() == PatientTypeEnum.PROCHE) {
				patientService.delete(pt);
				activityServices.createActivity(new Date(),"Delete","Delete Patient ID : "+pt.getId(),globalVariables.getConnectedUser());
				LOGGER.info("Delete Patient by ID : "+pt.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
				response.put("deleted", Boolean.TRUE);
			} else {
				activityServices.createActivity(new Date(),"Add","Cannot Delete Patient ID : "+pt.getId(),globalVariables.getConnectedUser());
				LOGGER.error("Cannot Patient ID : "+pt.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
				response.put("NOt authorized to delete Your self now", Boolean.FALSE);
			}

		} else {
			response.put("nomatch", Boolean.FALSE);
		}
		return response;
	}

	// Updating a patient by id. %OK%
	@PostMapping("/updatepatient/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	@ResponseBody
	public ResponseEntity<?> updatePatient(@Valid @PathVariable Long id,
			@Valid @RequestBody PatientRequest patient) throws Exception {


		Optional<Patient> pat = patientRepo.getPatientByUserId(globalVariables.getConnectedUser().getId(), id);
		if (pat.isPresent()) {

			try {
				Patient pt = patientRepo.getById(id);
				Ville ville = villeRepository.findById(patient.getVilleId())
						.orElseThrow(() -> new BadRequestException(
								"Ville not found for this id :: " + patient.getVilleId()));

				pt.setNom_pat(patient.getNom_pat());
				pt.setPrenom_pat(patient.getPrenom_pat());
				pt.setDateNaissance(patient.getDateNaissance());
				pt.setAdresse_pat(patient.getAdresse_pat());
				pt.setCodePost_pat(patient.getCodePost_pat());
				pt.setMatricule_pat(patient.getMatricule_pat());
				pt.setCivilite_pat(patient.getCivilite_pat());
				pt.setPatientEmail(patient.getPatientEmail());
				pt.setPatientTelephone(patient.getPatientTelephone());
				pt.setPlaceOfBirth(patient.getPlaceOfBirth());
				pt.setMutuelNumber(patient.getMutuelNumber());;
				pt.setVille(ville);
				pt.setPatient_type(patient.getPatient_type());

				Patient updatedpt = patientRepo.save(pt);
				activityServices.createActivity(new Date(),"Update","Update Patient ID : "+pt.getId(),globalVariables.getConnectedUser());
				LOGGER.info("Update Patient ID : "+pt.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
				return ResponseEntity.ok(mapper.map(updatedpt, Patient.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ResponseEntity.ok(new ApiResponse(false, "Pas de Patient avec ce id :: " + id));
		}
		return null;

	}

	@GetMapping("/getmypatientaccount") // %OK%
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	@ResponseBody
	public ResponseEntity<PatientResponse> getPatientByUserId() throws Exception {
	
		Patient patient = patientService.getPatientMoiByUserId(globalVariables.getConnectedUser().getId());
		System.out.println(patient.getId());
		mapper.typeMap(Patient.class, PatientResponse.class)
		.addMapping(src -> src.getVille().getId_ville(), PatientResponse::setVilleId);

  		PatientResponse patientResponse = mapper.map(patient, PatientResponse.class);
			activityServices.createActivity(new Date(),"Read","Consulting personal Patient Account : "+patient.getId(),globalVariables.getConnectedUser());
			LOGGER.info("Consulting Personal Patient Account ID : "+patient.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
  		return ResponseEntity.ok(patientResponse);
	
	}

	@GetMapping("/getallproch")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	@ResponseBody
	public ResponseEntity<?> findALLProchByUserId() throws Exception {

		activityServices.createActivity(new Date(),"Read","Consulting All Proch Account",globalVariables.getConnectedUser());
		LOGGER.info("Consulting All Proch Account, UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return ResponseEntity.ok(patientService.findALLProchByUserId(globalVariables.getConnectedUser().getId()));

	}
  	// share folder with a doctor :
  @PostMapping("/sharedoc/{idmed}/{iddoss}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	public ResponseEntity<?> ShareAccesstoFolder(@Valid @PathVariable Long idmed,@Valid @PathVariable("iddoss") Long iddossier) throws Exception{
	
			Patient patient=patientRepo.findPatientByUserIdandDossMedicale(globalVariables.getConnectedUser().getId(),iddossier).orElseThrow(()->new Exception("NO MATCHING FOUND FOR THAT USER !"));

			activityServices.createActivity(new Date(),"Update","Sharing Medical Folder ID : "+iddossier+" with Medecin : "+idmed,globalVariables.getConnectedUser());
			LOGGER.info("Sharing Medical Folder ID : "+iddossier+" with Medecin : "+idmed+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
			return ResponseEntity.ok(patientService.ShareMedecialFolder(patient.getDossierMedical().getId_dossier(),idmed));

	}
  
  	@GetMapping("/getDossierIdByPatientId/{patientId}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT')")
	@ResponseBody
	public ResponseEntity<Long> getDossierIdByPatientId(@PathVariable("patientId") Long patientId) {
	    Patient patient = patientRepo.findById(patientId)
	            .orElseThrow(() -> new BadRequestException("Patient not found with id: " + patientId));
	    Long dossierId = patient.getDossierMedical().getId_dossier();
	    return new ResponseEntity<>(dossierId, HttpStatus.OK);
	}
  
	@GetMapping("/getall")
	public List<Patient> findALLPatientByUserId() throws Exception {
    
		activityServices.createActivity(new Date(),"Read","Consulting All Patient Account",globalVariables.getConnectedUser());
		LOGGER.info("Consulting All Patient Account, UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return patientService.findALLPatientByUserId(globalVariables.getConnectedUser().getId());
	}

}
