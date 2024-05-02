package com.clinitalPlatform.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.clinitalPlatform.models.Specialite;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.services.SpecialiteServiceImpl;

@RestController
@RequestMapping("/api/specialites")
public class SpecialiteController {
	 @Autowired
	    private SpecialiteServiceImpl specialiteService;

	 @PostMapping("/ajouter")
	    public ResponseEntity<?> ajouterSpecialite(@RequestBody Specialite specialite) {
	        // Vérifier si le libellé de la spécialité existe déjà
	        if (specialiteService.existsByLibelle(specialite.getLibelle())) {
	        	return ResponseEntity.ok(new ApiResponse(false, "Le libellé de la spécialité existe déjà"));
	            
	        }
	        
	        // Ajouter la spécialité si le libellé n'existe pas déjà
	        Specialite nouvelleSpecialite = specialiteService.ajouterSpecialite(specialite);
	        return ResponseEntity.status(HttpStatus.CREATED)
	                             .body(new ApiResponse(true, "Spécialité ajoutée avec succès", nouvelleSpecialite));
	    }
}
