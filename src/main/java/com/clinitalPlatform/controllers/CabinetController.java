package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Cabinet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.services.CabinetServiceImpl;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.util.GlobalVariables;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cabinet/")
public class CabinetController {

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
	public ResponseEntity<?> getAllCabinets() {
		try {
			LOGGER.info("Get all cabinet from db start");

			List<Cabinet> cabinets = cabinetrep.findAll();
			LOGGER.info("Get all cabinet from db");
			if (cabinets.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Aucun cabinet trouvé.");
			}
			return ResponseEntity.ok(cabinets);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur interne du serveur : " + e.getMessage());
		}
	}


	@GetMapping("/cabinetById/{id}")
	public ResponseEntity<?> findById(@PathVariable Long id) throws Exception{
		return ResponseEntity.ok(cabservice.findById(id));
	}

    @GetMapping("/cabinetByName/{name}")
	public ResponseEntity<?> getCabinetByName(@PathVariable String name) throws Exception {
		return ResponseEntity.ok(cabservice.findByName(name));
	}

	//Recherche du cabinet par Medecin ID
	@GetMapping("/medecin/{id}")
	public ResponseEntity<List<Cabinet>> getAllCabinetsByMedecinId(@PathVariable Long id) {
		List<Cabinet> cabinets = cabservice.getAllCabinetsByMedecinId(id);
		return ResponseEntity.ok(cabinets);
	}






}
