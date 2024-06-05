package com.clinitalPlatform.controllers;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import com.clinitalPlatform.services.*;
import com.clinitalPlatform.services.interfaces.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clinitalPlatform.repository.CabinetMedecinRepository;
import com.clinitalPlatform.models.DocumentsCabinet;
import com.clinitalPlatform.payload.request.DocumentsCabinetRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.repository.DocumentsCabinetRepository;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.CabinetMedecinsSpace;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.CabinetRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/med/")
public class MedecinController {
	private final FileService fileService;
	
	@Autowired
	private MedecinServiceImpl medecinService;
	
	@Autowired
	MedecinRepository medrepository;
	
	@Autowired
	private CabinetRepository cabrepos;
	
	@Autowired 
	private ActivityServices activityServices;
	
	@Autowired
    GlobalVariables globalVariables;
	
	@Autowired
	private CabinetMedecinServiceImpl medcabinetservice;
	
	@Autowired
	private CabinetServiceImpl cabservice;
	
	@Autowired
	private CabinetMedecinRepository cabmedrep;
	
	@Autowired
	private DocumentsCabinetServices docservices;
	
	@Autowired
	private DocumentsCabinetRepository doccabrepository;
	
	
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());

    public MedecinController(FileServiceImpl fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/addMedtoExistcabinet")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public CabinetMedecinsSpace AddMedtoCabinet(@Valid @RequestBody CabinetRequest cabinetreq) throws Exception {
	    Medecin medecin = medecinService.findById(cabinetreq.getCabinetmedecin().getMedecin_id());
	    Optional<Cabinet> cabinetOptional = cabrepos.findById(cabinetreq.getCabinetmedecin().getCabinet_id());
	    
	    if (cabinetOptional.isEmpty()) {
	        throw new Exception("Cabinet not found");
	    }
	    Cabinet cabinet = cabinetOptional.get();
	    medecin.setStepsValidation(medecin.getStepsValidation() + 1);
	    medrepository.save(medecin);
	    
	    CabinetMedecinsSpace Cabmed = medcabinetservice.addCabinetMedecinsSpace(cabinetreq.getCabinetmedecin(), cabinet, medecin);
	    activityServices.createActivity(new Date(),"Add","Add a Medecin ID "+ medecin.getId()+" to  Cabinet By Connected Medecin Admin",globalVariables.getConnectedUser());
        LOGGER.info("Add Medecin ID "+ medecin.getId()+" to Cabinet ID: "+cabinet.getId_cabinet()+" , by Connected, User ID  : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
	    return Cabmed;
	}
	
	@PostMapping("/addcabinet")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public ResponseEntity<?> Addcabinet(@Valid @RequestBody CabinetRequest cabinetreq) throws Exception {
		Medecin medecin = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		Cabinet cabinet = cabservice.create(cabinetreq,medecin);
		cabinetreq.setId_cabinet(cabinet.getId_cabinet());
		medecin.setStepsValidation(medecin.getStepsValidation()+1);
		medrepository.save(medecin);
		CabinetMedecinsSpace Cabmed = medcabinetservice.addCabinetMedecinsSpace(cabinetreq.getCabinetmedecin(),cabinet,medecin );
		activityServices.createActivity(new Date(),"Add","Add new Cabinet By Connected Medecin Admin",globalVariables.getConnectedUser());
            LOGGER.info("Add new Cabinet ID: "+cabinet.getId_cabinet()+" , by Connected, User ID  : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return ResponseEntity.ok(Cabmed);

	}

	// delete a cabinet :
	@DeleteMapping(path = "/deletecabinet/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public ResponseEntity<?> DeleteCabinet(@Valid @PathVariable long id) throws Exception {
	
			Medecin med = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
			Optional<CabinetMedecinsSpace> isAdminMed = cabmedrep.isAdmin(med.getId(), id);
			if (isAdminMed.isPresent()) {
				Cabinet cabinet = cabrepos.getById(id);
				medcabinetservice.deleteCabinetMedecins(id);
				cabrepos.DeleteCabinetbyID(id);
				
				activityServices.createActivity(new Date(),"Delete","Delete a  Cabinet ID:"+cabinet.getId_cabinet()+" By Connected Medecin Admin",globalVariables.getConnectedUser());
	            LOGGER.info("Delete a Cabinet ID: "+cabinet.getId_cabinet()+" , by Connected, User ID  : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
				return ResponseEntity.ok("Cabinet has been deleted successefully");
			} else
				return (ResponseEntity<?>) ResponseEntity.ok("You are not allowed");

		}
	
	@GetMapping("/mycabinets")
	@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
	public ResponseEntity<?> AllCabinetByCurrentMedecin() throws Exception{
	
		Medecin med=medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		List<Cabinet> cabinets=cabservice.allCabinetsByMedID(med.getId());

		activityServices.createActivity(new Date(), "Read", "Consult all Cabinets where work this Medecin with ID "+med.getId(), globalVariables.getConnectedUser());
        LOGGER.info("Consult all Cabinets where work this Medecin with ID "+med.getId()+" , By User : "+globalVariables.getConnectedUser());

		return ResponseEntity.ok(cabinets) ;
	}

	// add docs to Cabinet :
	@PostMapping(path = "/addCabinetDoc")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public ResponseEntity<?> addCabinetDoc(@RequestParam String document,
				@RequestParam MultipartFile docFile) throws Exception {
		ObjectMapper om = new ObjectMapper();

		DocumentsCabinetRequest documentReq = om.readValue(document, DocumentsCabinetRequest.class);

		try {
			UserDetailsImpl CurrentUser= (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Medecin med= medrepository.getMedecinByUserId(CurrentUser.getId());

			 String fileNameWithExtension = docFile.getOriginalFilename();

	        // ----Add document :
	        DocumentsCabinet savedDoc = docservices.create(documentReq);
	        savedDoc.setFichier_doc(fileNameWithExtension);
			doccabrepository.save(savedDoc);
			med.setStepsValidation(med.getStepsValidation()+1);
			medrepository.save(med);
			fileService.save(docFile);
			activityServices.createActivity(new Date(),"Add","Add New document ID:"+savedDoc.getId()+", for Cabinet ID : "+documentReq.getId_cabinet(),globalVariables.getConnectedUser());
			LOGGER.info("Add New document ID:"+savedDoc.getId()+", for Cabinet ID : "+documentReq.getId_cabinet()+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
			return ResponseEntity.ok(new ApiResponse(true, "Document created successfully!"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(new ApiResponse(false, "Document not created!"+e.getMessage()));

		}
     }


//	 @PostMapping("/upload")
//	 @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
//	 public void upload(@RequestParam  MultipartFile file){
//		 fileService.save(file);
//
//	 }

	@GetMapping("/get/{file}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public ResponseEntity<Resource> getFile(@PathVariable String file){
		 Resource resource= fileService.getFile(file);
		 if (resource!=null){
			 return ResponseEntity.ok()
					 .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+resource.getFilename()+"\"")
					 .contentType(MediaType.APPLICATION_OCTET_STREAM)
					 .body(resource);
		 }

        return ResponseEntity.internalServerError().build();
    }




}
