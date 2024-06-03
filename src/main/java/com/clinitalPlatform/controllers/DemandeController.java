package com.clinitalPlatform.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinitalPlatform.enums.DemandeStateEnum;
import com.clinitalPlatform.models.Demande;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.dto.DemandeDTO;
import com.clinitalPlatform.services.DemandeServiceImpl;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/demandes/")
public class DemandeController {
	
	@Autowired
	private DemandeServiceImpl demandeService;
	
	@Autowired
	ClinitalModelMapper modelMapper;
	
	@Autowired
	GlobalVariables globalVariables;
	
	// A method that creates a demande.
	@PostMapping("create")
	public ResponseEntity<?> create(@RequestBody DemandeDTO demande){
		
		return ResponseEntity.ok(demandeService.create(demande));
	}

	// Updating a demande by id. 
	@PutMapping("update/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<DemandeDTO> update(@RequestBody DemandeDTO demande,@PathVariable Long id) throws Exception{
		
		DemandeDTO demande2 = demandeService.update(demande, id);
		
		return ResponseEntity.accepted().body(demande2);
	}
	
	// A method that returns all the demandes. 
	@GetMapping("all")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<List<DemandeDTO>> all(){
	
		return ResponseEntity.ok(demandeService.findAll());
	}
	
	// A method that returns a demande by id.
	@GetMapping("findbyid/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<DemandeDTO> findById(@PathVariable Long id) throws Exception{
		
		return ResponseEntity.ok(demandeService.findById(id));
	}
	
	// A method that returns a demande by state.
	@GetMapping("demandebystate/{state}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<List<Demande>> findBySateDemande(@Validated @PathVariable DemandeStateEnum state) throws Exception{
		
		return ResponseEntity.ok(demandeService.findByState(state)
		.stream().map(demnd -> modelMapper.map(demnd, Demande.class))
		.collect(Collectors.toList()));
	}

	// A method that deletes a demande by id. 
	@DeleteMapping("delete/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<?> deleteById(@PathVariable Long id) throws Exception{
		demandeService.deleteById(id);
		return  ResponseEntity.ok(new ApiResponse(true, " Demande has been deleted successfully "+HttpStatus.OK)  );
	}

	// Valider la demande : 
	@PostMapping("validerdmd/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Demande> ValidateDemande(@Validated @RequestBody DemandeDTO valide,@PathVariable Long id) throws Exception{
		
		Demande demande2 = demandeService.validate(valide.getValidation(),id);
		
		return ResponseEntity.accepted().body(demande2);
	}
	
	@PutMapping("updateDemandeStateByUserId/{newState}")
	@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
	public ResponseEntity<?> updateDemandeStateByUserId(@PathVariable int newState)  throws Exception {
	  
	        // Récupérer l'ID de l'utilisateur connecté à partir de globalVariables
	        Long userId = globalVariables.getConnectedUser().getId();
	        
	        // Appeler la méthode du service pour mettre à jour l'état de la demande
	        Demande updatedDemande = demandeService.updateDemandeStateByUserId(userId, newState);
	        
	        return ResponseEntity.ok(updatedDemande);    
	}

}
