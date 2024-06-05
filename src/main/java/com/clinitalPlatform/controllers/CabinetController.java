package com.clinitalPlatform.controllers;

import com.clinitalPlatform.enums.CabinetDocStateEnum;
import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.DocumentsCabinet;
import com.clinitalPlatform.repository.DocumentsCabinetRepository;
import com.clinitalPlatform.services.DocumentsCabinetServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.clinitalPlatform.services.CabinetServiceImpl;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.util.GlobalVariables;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cabinet")
public class CabinetController {
	@Autowired
	private DocumentsCabinetServices docservices;

    @Autowired
    CabinetRepository cabinetrep;
    
	@Autowired
	MedecinRepository medrepo;
	
	@Autowired
    GlobalVariables globalVariables;
	
	 @Autowired
	 CabinetServiceImpl cabservice;
	
	public final Logger LOGGER=LoggerFactory.getLogger(this.getClass());
	
    @GetMapping("/allcabinet")
	public ResponseEntity<?> getAllCabinets() throws Exception {
		return ResponseEntity.ok(cabservice.findAll());
	}
	
	@GetMapping("/cabinetById/{id}")
	public ResponseEntity<?> findById(@PathVariable Long id) throws Exception{
		return ResponseEntity.ok(cabservice.findById(id));
	}

    @GetMapping("/cabinetByName/{name}")
	public ResponseEntity<?> getCabinetByName(@PathVariable String name) throws Exception {
		return ResponseEntity.ok(cabservice.findByName(name));
	}
	@PutMapping(path = "/{documentId}/validate")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public ResponseEntity<?> validateDocument(@PathVariable("documentId") long documentId, @RequestParam CabinetDocStateEnum validationState) {
		try {
			DocumentsCabinet validatedDocument =docservices.validateDocument(documentId, validationState);
			return ResponseEntity.ok(validatedDocument);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to validate document: " + e.getMessage());
		}
	}
	@GetMapping("/byMedecin/{medecinId}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
	public ResponseEntity<?> getCabinetsByMedecinId(@PathVariable("medecinId") long medecinId) {
		try {
			List<Cabinet> cabinets = cabservice.allCabinetsByMedID(medecinId);
			return ResponseEntity.ok(cabinets);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve cabinets: " + e.getMessage());
		}
	}
	}



