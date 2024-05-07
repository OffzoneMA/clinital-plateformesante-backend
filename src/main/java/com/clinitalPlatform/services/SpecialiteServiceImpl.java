package com.clinitalPlatform.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.dto.SpecialiteDTO;
import com.clinitalPlatform.models.Specialite;
import com.clinitalPlatform.repository.SpecialiteRepository;
import com.clinitalPlatform.services.interfaces.SpecialiteService;
import com.clinitalPlatform.util.ClinitalModelMapper;

@Transactional
@Service
public class SpecialiteServiceImpl implements SpecialiteService{
	
	@Autowired
	private SpecialiteRepository specialiteRepository;
	
	@Autowired
	private ClinitalModelMapper modelMapper;

	@Override
	public List<SpecialiteDTO> findAll() {
		return specialiteRepository.findAll()
				.stream()
				.map(sp->modelMapper.map(sp, SpecialiteDTO.class))
				.collect(Collectors.toList());
	}
	public boolean existsByLibelle(String libelle) {
        return specialiteRepository.existsByLibelle(libelle);
    }
	 public Specialite ajouterSpecialite(Specialite specialite) {
	        // Vous pouvez ajouter ici d'autres validations avant d'ajouter la spécialité
	        return specialiteRepository.save(specialite);
	    }
}
