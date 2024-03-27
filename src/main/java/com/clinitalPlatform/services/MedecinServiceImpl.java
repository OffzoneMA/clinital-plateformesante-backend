package com.clinitalPlatform.services;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

@Transactional
@Service
public class MedecinServiceImpl implements MedecinService {

	@Autowired
	private MedecinRepository medecinRepository;

	@Autowired
	private ClinitalModelMapper clinitalModelMapper;

	@Autowired
	ActivityServices activityServices;


	@Autowired
	EmailSenderService emailSenderService;

	@Autowired
	DemandeServiceImpl demandeServiceImpl;

	@Autowired
    GlobalVariables globalVariables;

	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	
	@Override
	public Medecin findById(Long id) throws Exception {
		
		Medecin med = medecinRepository.findById(id).orElseThrow(() -> new Exception("Medecin not found"));
		
		return clinitalModelMapper.map(med, Medecin.class);
	}
	
	@Override
	public Medecin getMedecinByUserId(long id) throws Exception {

		Medecin med = medecinRepository.getMedecinByUserId(id);

		return clinitalModelMapper.map(med, Medecin.class);
	}

	

}
