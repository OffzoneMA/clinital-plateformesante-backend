package com.clinitalPlatform.controllers;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.clinitalPlatform.services.CabinetMedecinServiceImpl;
import com.clinitalPlatform.services.CabinetServiceImpl;
import com.clinitalPlatform.repository.CabinetMedecinRepository;
import com.clinitalPlatform.models.DocumentsCabinet;
import com.clinitalPlatform.payload.request.DocumentsCabinetRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.services.DocumentsCabinetServices;
import com.clinitalPlatform.repository.DocumentsCabinetRepository;
import com.clinitalPlatform.services.OrdonnanceServiceImpl;
import com.clinitalPlatform.models.Ordonnance;
import com.clinitalPlatform.payload.request.OrdonnanceRequest;
import com.clinitalPlatform.repository.OrdonnanceRepository;
import com.clinitalPlatform.util.PDFGenerator;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.dto.SpecialiteDTO;
import com.clinitalPlatform.services.interfaces.SpecialiteService;
import com.clinitalPlatform.dto.OrdonnanceDTO;
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
	
	@Autowired
	private OrdonnanceServiceImpl OrdonnanceServices;
	
	@Autowired
	private OrdonnanceRepository ordonnanceRepository;
	
	@Autowired
	private PDFGenerator pdfgenerator;
	
	@Autowired
	ClinitalModelMapper mapper;
	
	@Autowired
	private SpecialiteService specialiteService;
	
	
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	
	@GetMapping("/medecins")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public Iterable<Medecin> medecins() throws Exception {
		
		return medrepository.findAll().stream().filter(med->med.getIsActive()==true).collect(Collectors.toList());

	}
	
	// Get Medecin By Id : %OK%
	@GetMapping("/medById/{id}")
	public ResponseEntity<Medecin> getMedecinById(@PathVariable(value="id") Long id) throws Exception {
			
			return ResponseEntity.ok(mapper.map(medecinService.findById(id), Medecin.class));
	}

	// Get Medecin y his name : %OK%
	@GetMapping("/medByName")
	@ResponseBody
	public List<Medecin> findMedByName(@RequestParam String nomMed) throws Exception {
			
			return medrepository.getMedecinByName(nomMed).stream().filter(med->med.getIsActive()==true).collect(Collectors.toList());
	}
	
	// end point for getting Doctor by Name or speciality and city : %OK%
	@GetMapping("/medByNameOrSpecAndVille")
	@ResponseBody
	public Iterable<Medecin> medByNameOrSpecAndVille(@RequestParam String ville,
				@RequestParam String search) throws Exception {
					
			return medrepository.getMedecinBySpecialiteOrNameAndVille( search,ville).stream()
			.filter(med->med.getIsActive()==true).collect(Collectors.toList());
	}

	// end point for getting Doctor by Name and speciality : %OK%
	@GetMapping("/medByNameAndSpec")
	public Iterable<Medecin> findMedSpecNameVille(@RequestParam String name,
				@RequestParam String search) throws Exception {
					
		return medrepository.getMedecinBySpecialiteAndName(search, name).stream()
		.filter(med->med.getIsActive()==true).collect(Collectors.toList());
	}

	// end point for getting Doctor by Name or speciality : %OK%
	@GetMapping("/medByNameOrSpec")
	public Iterable<Medecin> findMedSpecName(@RequestParam String search) throws Exception {
			
			return medrepository.getMedecinBySpecOrName(search).stream().filter(med->med.getIsActive()==true).collect(Collectors.toList());

	}

	// end point for getting Doctor By city : %OK%
	@GetMapping("/medByVille")
	public Iterable<Medecin> findMedByVille(@RequestParam Long id_ville) throws Exception {
			
			return medrepository.getMedecinByVille(id_ville).stream().filter(med->med.getIsActive()==true).collect(Collectors.toList());
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
				
				activityServices.createActivity(new Date(),"Delete","Add a  Cabinet ID:"+cabinet.getId_cabinet()+" By Connected Medecin Admin",globalVariables.getConnectedUser());
	            LOGGER.info("Delete a Cabinet ID: "+cabinet.getId_cabinet()+" , by Connected, User ID  : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
				return ResponseEntity.ok("Cabinet has been deleted successefully");
			} else
				throw new BadRequestException("You are Not Allowed");

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
			activityServices.createActivity(new Date(),"Add","Add New document ID:"+savedDoc.getId()+", for Cabinet ID : "+documentReq.getId_cabinet(),globalVariables.getConnectedUser());
			LOGGER.info("Add New document ID:"+savedDoc.getId()+", for Cabinet ID : "+documentReq.getId_cabinet()+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
			return ResponseEntity.ok(new ApiResponse(true, "Document created successfully!"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(new ApiResponse(false, "Document not created!"+e.getMessage()));
			
		}
     }
	
	//=================================================================================================
	// ORDONNANCE :

	@PostMapping("/addordonnance")
		public ResponseEntity<?> AddNewOrdonnance(@Valid @RequestBody OrdonnanceRequest creq){

			try {
				
				Medecin med = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());

				Ordonnance consul=OrdonnanceServices.create(creq, med);

				String fileName = consul.getId_ordon()+"-DOSS"+ consul.getDossier().getId_dossier()+"-MedID"+med.getId();

				pdfgenerator.GenartePdfLocaly(consul, fileName, "Patientdoc");

				return ResponseEntity.ok(consul);

			} catch (Exception e) {
				e.printStackTrace();
			return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
			}
			
		}

	//update consulation :
	@PostMapping("/updateordonnance")
	public ResponseEntity<?> Updateordonnance(@Valid @RequestBody OrdonnanceRequest creq){

		try {
			
			Medecin med = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());

			OrdonnanceDTO consul=OrdonnanceServices.update(creq,med);

			return ResponseEntity.ok(consul);

		} catch (Exception e) {
			e.printStackTrace();
		return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
		}
		
	}
	
	@GetMapping(path = "/allordonnacebymed")
	public ResponseEntity<?> findallByIdMedecin(){

	try {	
		Medecin med = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		List<OrdonnanceDTO> allord=OrdonnanceServices.findAllByMed(med);

		return ResponseEntity.ok(allord);
	} catch (Exception e) {
		e.printStackTrace();
		return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));

	}
	}
	
	@GetMapping(path = "/allordonnace")
	public ResponseEntity<?> findall(){

	try {	
		List<OrdonnanceDTO> allord=OrdonnanceServices.findAll();

		return ResponseEntity.ok(allord);
	} catch (Exception e) {
		e.printStackTrace();
		return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));

	}
	}
	
	@GetMapping(path = "/findordi/{id}")
	public ResponseEntity<?> getByIdMedecin(@Valid @PathVariable(value = "id")Long idordo){

		try {
			
			Medecin med = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
			OrdonnanceDTO consul=OrdonnanceServices.findById(idordo,med);

			return ResponseEntity.ok(consul);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
		}
	}
	
	// Delete Coonsultation : 
	@DeleteMapping(path = "/deleteordonnance/{id}")
	public ResponseEntity<?> DeleteOrdonnance(@Valid @PathVariable Long id){
		
	try {
		Ordonnance ordonnance=ordonnanceRepository.findById(id).orElseThrow(()->new Exception("No 	matching found for this ordonnance"));

		Boolean IsDeleted = OrdonnanceServices.deleteById(ordonnance)?true:false;

		return IsDeleted ? ResponseEntity.ok(new ApiResponse(true, "ordonnance has been deleted seccussefully")):ResponseEntity.ok(new ApiResponse(true, "Consultation cannot be deleted"));

	} catch (Exception e) {
		e.printStackTrace();
		return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
	}

	}
	
	// show data by medecin access right
	@GetMapping(path = "/findordonnance/{iddoss}/{idordo}")
	public ResponseEntity<?> findOrdoByIdMedecin(@Valid @PathVariable(value = "idordo")Long id,@Valid @PathVariable Long iddoss){

	try {
		
		Medecin med = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		return ResponseEntity.ok(OrdonnanceServices.findByIdMedandDossierId(med,iddoss,id));
		
	} catch (Exception e) {
		e.printStackTrace();
		return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
	}

	}
	
	@GetMapping("/getAllSpec")
	public ResponseEntity<List<SpecialiteDTO>> findAll() throws Exception {

		return ResponseEntity.ok(specialiteService.findAll());
	}

	
}
