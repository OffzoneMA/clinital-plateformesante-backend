package com.clinitalPlatform.controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.dto.SecretaireDTO;
import com.clinitalPlatform.models.Secretaire;
import com.clinitalPlatform.payload.request.SecritaireRequest;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.SecretaireServiceImpl;
import com.clinitalPlatform.util.GlobalVariables;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sec")
public class SecretaireController {
	
	@Autowired
	private SecretaireServiceImpl secretaireService;

	@Autowired
    GlobalVariables globalVariables;

	@Autowired
	private ActivityServices activityServices;

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@PostMapping("/create")
	public ResponseEntity<SecretaireDTO> Create(@RequestBody SecritaireRequest secretaire){
		
		secretaireService.create(secretaire);
		return new ResponseEntity<SecretaireDTO>(HttpStatus.CREATED);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<Secretaire> Update(@RequestBody SecritaireRequest secretaire,@PathVariable Long id) throws Exception{
	Secretaire sec =secretaireService.update(secretaire, id);
		return ResponseEntity.accepted().body(sec);
	}
	
	
	
	@GetMapping("/all")
	public ResponseEntity<List<Secretaire>> findAll(){
		return ResponseEntity.ok(secretaireService.findAll());
	}
	
	@GetMapping("/id/{id}")
	public ResponseEntity<Secretaire> findById(@PathVariable Long id) throws Exception{
		return ResponseEntity.ok(secretaireService.findById(id));
	}


}
