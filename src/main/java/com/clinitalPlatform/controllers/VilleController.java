package com.clinitalPlatform.controllers;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.dto.VilleDTO;
import com.clinitalPlatform.models.Ville;
import com.clinitalPlatform.repository.VilleRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ville")
public class VilleController {

	@Autowired
	VilleRepository villerepo;

	@Autowired
	ClinitalModelMapper mapper;

	@Autowired
	ActivityServices activityServices;

	@Autowired
	GlobalVariables globalVariables;


	@GetMapping("/allvilles")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	Iterable<VilleDTO> villes() throws Exception {

		return villerepo.findAll().stream().map(ville -> mapper.map(ville, VilleDTO.class))
				.collect(Collectors.toList());
	}
	@GetMapping("/{id}")
	public ResponseEntity<String> getVilleNameById(@PathVariable("id") Long id) {
	    Optional<Ville> villeOptional = villerepo.findById(id);
	    if (villeOptional.isPresent()) {
	        Ville ville = villeOptional.get();
	        String villeName = ville.getNom_ville(); // Supposons que getNom() retourne le nom de la ville
	        return ResponseEntity.ok(villeName);
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}

}
