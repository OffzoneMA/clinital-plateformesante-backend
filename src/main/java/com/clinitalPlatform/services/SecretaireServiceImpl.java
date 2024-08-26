package com.clinitalPlatform.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.dto.SecretaireDTO;
import com.clinitalPlatform.dto.UserDTO;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.Secretaire;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.SecritaireRequest;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.SecretaireRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.services.interfaces.SecretaireService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

@Transactional
@Service
public class SecretaireServiceImpl implements SecretaireService{

	@Autowired
	private SecretaireRepository secretaireRepository;

	@Autowired
	private ClinitalModelMapper clinitalModelMapper;

	@Autowired
	private CabinetRepository cabinetrepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ClinitalModelMapper ModelMapper;

	@Autowired
    GlobalVariables globalVariables;

	@Autowired
	private ActivityServices ActivityServices;
	
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	

	@Override
	public Secretaire create(SecritaireRequest sec) {
try {
		Secretaire secUser = new Secretaire();
		
			return this.SaveSecretaire(sec, secUser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
return null;
		
	}

	@Override
	public Secretaire update(SecritaireRequest sec, Long id) throws Exception {
		Cabinet cabinet=cabinetrepo.findById(sec.getCabinetid()).orElseThrow(()->new Exception("No Matching Cainet Found"));
		Optional<Secretaire> secretaire = secretaireRepository.findById(id);
		
		if(secretaire.isPresent()) {
			ActivityServices.createActivity(new Date(), "Update","Update Secretaire ID : "+secretaire.get().getId(),globalVariables.getConnectedUser());
			LOGGER.info("Add New Secretaire ID : "+secretaire.get().getId()+" to Cabinet ID : "+cabinet.getId_cabinet()+", User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
			
			return this.SaveSecretaire(sec, secretaire.get());
			
		}else
			throw new Exception("Failed to update");
	}

	@Override
	public List<Secretaire> findAll() {
		try {ActivityServices.createActivity(new Date(), "Read", "Consult All Secretaire ",globalVariables.getConnectedUser());
		
			LOGGER.info("Consult All Secretaire, UserID : " + globalVariables.getConnectedUser().getId());
		
		return secretaireRepository
				.findAll()
				.stream()
				.map(secretaire->clinitalModelMapper.map(secretaire, Secretaire.class))
				.collect(Collectors.toList());} 
				catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Secretaire findById(Long id) throws Exception {
	 Secretaire secretaire= secretaireRepository.findById(id).orElseThrow(()-> new Exception("Secretaire not found"));
	 ActivityServices.createActivity(new Date(), "Read", "Consult Secretaire ID:"+id,globalVariables.getConnectedUser());
	LOGGER.info("Consult Secretaire ID:"+id+", UserID : " + globalVariables.getConnectedUser().getId());
	 return secretaire;
	}

	@Override
	public boolean deleteSecretaireById(Long id,long idcabinet) throws Exception {
		Optional<Secretaire>  secretaire = secretaireRepository.findById(id);
		if(secretaire.isPresent()) {
			ActivityServices.createActivity(new Date(), "Delete", "Delete Secretaire Secretaire ID:"+id+" to Cabinet ID :"+idcabinet,globalVariables.getConnectedUser());
			LOGGER.info("Delete Secretaire Secretaire ID:"+id+" to Cabinet ID :"+idcabinet+", UserID : " + globalVariables.getConnectedUser().getId());
			return SecretaireRepository.deleteByIdfromCabinet(id,idcabinet);
		}else 
		ActivityServices.createActivity(new Date(), "Warning", "Cannot Delete Secretaire Secretaire ID:"+id,globalVariables.getConnectedUser());
			LOGGER.warn("Cannot Delete Secretaire ID:"+id+", UserID : " + globalVariables.getConnectedUser().getId());
		throw new Exception("Secretaire not found");
		
	}

	public Secretaire SaveSecretaire(SecritaireRequest sec, Secretaire secretaire2 ) throws Exception{

		// UserDetailsImpl userdetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
// check if any Cabinet exist already in DB with the given ID.
			Cabinet cabinet=cabinetrepo.findById(sec.getCabinetid()).orElseThrow(() -> new BadRequestException("Cabinet not found for this id ::" + sec.getCabinetid()));

			// check if any Cabinet exist already in Relation with this Secretaire
			Optional<Cabinet> iscabinet = cabinetrepo.isCabinetSecret(secretaire2.getId(), cabinet.getId_cabinet());
			//check if this cabinet exist for this secretaire yes skip else add it
			
			if(!iscabinet.isPresent()){
				secretaire2.getCabinet().add(cabinet);
				ActivityServices.createActivity(new Date(), "Add","Add New Secretaire",globalVariables.getConnectedUser());
				LOGGER.info("Add New Secretaire ID : "+secretaire2.getId()+" to Cabinet ID : "+cabinet.getId_cabinet()+", User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

			}else{
				ActivityServices.createActivity(new Date(), "Warning", "You cant Add this Secretaire to Cabinet ID :"+cabinet
				.getId_cabinet()+"Cabinet already exist for this Secretaire ID:"+secretaire2.getId(),globalVariables.getConnectedUser());
				LOGGER.warn("Add New Secretaire, UserID : " + globalVariables.getConnectedUser().getId());
				throw new BadRequestException("Cabinet already exist for this Secretaire ::" + secretaire2.getId());
			}

			// update Secretaire of this secretaire.
			secretaire2.setAdresse(sec.getAdresse());
			secretaire2.setDateNaissance(sec.getDateNaissance());
			secretaire2.setNom(sec.getNom());
			secretaire2.setPrenom(sec.getPrenom());
						
			Secretaire secri = secretaireRepository.save(secretaire2);
			
			return secri;

	}


	public List<Secretaire> findByIdCabinet(Long id) {
		try {
			return secretaireRepository
					.findSecretairesByCabinetId(id)
					.stream()
					.map(secretaire->clinitalModelMapper.map(secretaire, Secretaire.class))
					.collect(Collectors.toList());}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
}