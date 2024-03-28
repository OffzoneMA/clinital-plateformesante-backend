package com.clinitalPlatform.controllers;


import com.clinitalPlatform.dto.VilleDTO;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.VilleRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.stream.Collectors;

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

//	@Autowired
//	private UserRepository UserRepository;

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@GetMapping("/allvilles")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	Iterable<VilleDTO> villes() throws Exception {
	
		if(globalVariables.getConnectedUser()!=null){
			activityServices.createActivity(new Date(),"Read","LOADING All cities ",globalVariables.getConnectedUser());
		LOGGER.info("loading all villes "+ (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		}
		return villerepo.findAll().stream().map(ville -> mapper.map(ville, VilleDTO.class))
				.collect(Collectors.toList());
	}

}
