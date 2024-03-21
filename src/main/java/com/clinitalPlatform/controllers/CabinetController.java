package com.clinitalPlatform.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cabinet")
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


}
