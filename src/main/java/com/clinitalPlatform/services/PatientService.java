package com.clinitalPlatform.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.dao.IDao;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Transactional
@Service
@Primary
public class PatientService implements IDao<Patient> {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DossierMedicalRepository dossierMedicalRepository;
	
	@Autowired
    private ClinitalModelMapper modelMapper;

	@Autowired
	private ActivityServices ActivityServices;

	@Autowired
	private GlobalVariables globalVariables;
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());

	@Override
	public Patient create(Patient user){
		try {
		DossierMedical dossierMedical = new DossierMedical();
			dossierMedical.setAlchole(false);
			dossierMedical.setFumeur(false);
			dossierMedical.setAccesscode(null);
			dossierMedical.setDossierType(user.getPatient_type());
			dossierMedical.setNumDossier(null);
			dossierMedical.setTraitement(true);
			dossierMedicalRepository.save(dossierMedical);
			user.setDossierMedical(dossierMedical);
		// save activity update Patient 
		ActivityServices.createActivity(new Date(), "ADD", "Add New Patient", globalVariables.getConnectedUser());
		
			LOGGER.info("Add new Patient "+user.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return patientRepository.save((Patient) user);
		
	}

	@Override
	public void update(Patient o) {
		patientRepository.save(o);
	}

	@Override
	public void delete(Patient o) {
		patientRepository.deletePatient(o.getId());;
	}

	@Override
	public List<Patient> findAll() {
		return patientRepository.findAll();
	}

	@Override
	public Optional<Patient> findById(long id) {
		return patientRepository.findById(id);
	}
	
	public Patient getPatientMoiByUserId(long id){
		
		return patientRepository.getPatientMoiByUserId(id);

	}

	public List<Patient> findALLProchByUserId(long id){

		return patientRepository.findALLProchByUserId(id).stream()
		.map(pat -> modelMapper.map(pat, Patient.class)).collect(Collectors.toList());

	}


}
