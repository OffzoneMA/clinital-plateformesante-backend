package com.clinitalPlatform.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.clinitalPlatform.dto.CabinetDTO;
import com.clinitalPlatform.dto.MedecinDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Ville;
import com.clinitalPlatform.models.CabinetMedecinsSpace;

import com.clinitalPlatform.payload.request.CabinetRequest;
import com.clinitalPlatform.repository.VilleRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.services.interfaces.CabinetService;
import com.clinitalPlatform.util.ClinitalModelMapper;
@Transactional
@Service
public class CabinetServiceImpl implements CabinetService{
	
	@Autowired
	private CabinetRepository cabinetRepository;
	
	@Autowired
	private ClinitalModelMapper modelMapper;
	
	@Autowired
	private VilleRepository villerepo;
	@Autowired
	private MedecinRepository medrepo;
	
	public final Logger LOGGER=LoggerFactory.getLogger(this.getClass());

	@Override
	public List<Cabinet> findByName(String name) throws Exception {
		List<Cabinet> cabinets=cabinetRepository.findByNomContainingIgnoreCase(name)
		.stream().map(cabinet->modelMapper.map(cabinet, Cabinet.class) )
		.collect(Collectors.toList());
		return cabinets;
	}

	@Override
	public Optional<Cabinet> findById(Long id) throws Exception { 
		return cabinetRepository.findById(id);
	}
	
	@Override
	public List<Cabinet> findAll() throws Exception { 
		List<Cabinet> cabinets=cabinetRepository.findAll()
				.stream().map(cabinet->modelMapper.map(cabinet, Cabinet.class) )
				.collect(Collectors.toList());
				return cabinets;
	}
	
	@Override
	public Cabinet create(CabinetRequest cabinetreq,Medecin med) throws Exception {
		
		Ville ville= villerepo.findById(cabinetreq.getId_ville()).orElseThrow(()->new Exception("No matching ville"));
		Cabinet cabinet = new Cabinet();
		cabinet.setNom(cabinetreq.getNom());
		cabinet.setAdresse(cabinetreq.getAdresse());
		cabinet.setCode_post(cabinetreq.getCode_post());
		cabinet.setVille(ville);
		cabinet.setPhoneNumber(cabinetreq.getPhoneNumber());
		cabinet.setCreator(med);
		cabinet.setState(false);
		cabinetRepository.save(cabinet);
	
		return cabinet;
	}

	@Override
	public List<Cabinet> allCabinetsByMedID(Long id) throws Exception {
	  try {
		Medecin med=medrepo.findById(id).orElseThrow(()->new Exception("No Matching Medecin for this ID"));
		List<Cabinet> cabinets=cabinetRepository.getAllCabinetByIdMed(med.getId()).stream().map(cab->modelMapper.map(cab, Cabinet.class)).collect(Collectors.toList());
			
		return  cabinets;
	  } catch (Exception e) {
		throw new Exception(e.getMessage());
	  }
				
	}

	public List<Cabinet> getAllCabinetsByMedecinId(Long medecinId) {
		Medecin medecin = medrepo.findById(medecinId)
				.orElseThrow(() -> new RuntimeException("Medecin not found with ID: " + medecinId));
		return medecin.getCabinets().stream()
				.map(CabinetMedecinsSpace::getCabinet)
				.collect(Collectors.toList());
	}



}
