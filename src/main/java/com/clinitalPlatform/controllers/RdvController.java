package com.clinitalPlatform.controllers;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.repository.DocumentRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.repository.RdvRepository;
import com.clinitalPlatform.repository.SpecialiteRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.services.PatientService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

@org.springframework.transaction.annotation.Transactional
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/rdv")
public class RdvController {

	@Autowired
	RdvRepository rdvrepository;

	@Autowired
	ClinitalModelMapper mapper;

	@Autowired
	MedecinRepository medRepo;

	@Autowired
	MedecinServiceImpl medservice;

	@Autowired
	PatientRepository patientRepo;

	@Autowired
	PatientService patientService;

	@Autowired
	DocumentRepository docrepository;
	@Autowired
	SpecialiteRepository speciarepspo;

	@PersistenceContext
	private EntityManager entityManger;

	@Autowired
	GlobalVariables globalVariables;

	@Autowired
	private ActivityServices activityServices;

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());


	@GetMapping("medcin/patientId/{id}")
	@PreAuthorize("hasAuthority('ROLE_PATIENT')")
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

}

	

