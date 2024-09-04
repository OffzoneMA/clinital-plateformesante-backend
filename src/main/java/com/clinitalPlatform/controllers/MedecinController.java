package com.clinitalPlatform.controllers;


import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.clinitalPlatform.dto.*;
import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.*;
import com.clinitalPlatform.payload.response.AgendaResponse;
import com.clinitalPlatform.payload.response.GeneralResponse;
import com.clinitalPlatform.payload.response.HorairesResponse;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.services.*;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.util.PDFGenerator;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.services.interfaces.SpecialiteService;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.User;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/med/")
public class MedecinController {
	
	@Autowired
	private MedecinServiceImpl medecinService;
	
	@Autowired
	MedecinRepository medrepository;
	@Autowired
	PatientRepository patientRepository;
	
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
	@Autowired
	private MedecinScheduleServiceImpl medecinScheduleService;

	@Autowired
	private LangueserviceImpl langueservice;

	@Autowired
	private TarifRepository tarifRepository;
	@Autowired
	private SecretaireServiceImpl secretaireService;

	@Autowired
	private AssistantServiceImpl assistantService;

	@Autowired
	private MedecinNetworkService medecinNetworkService;
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());

	@Autowired
	private MedecinFilterService medecinFilterService;
	@Autowired
	private RdvRepository rdvRepository;

//start zakia
	@Autowired
	RendezvousService rendezvousService;

	@Autowired
	MedecinScheduleRepository medScheduleRepo;
	@Autowired
	private CabinetRepository cabinetRepository;
	@Autowired
	private SpecialiteRepository specialiteRepository;
	public static boolean checkday = false;
	//end zakia
	@GetMapping("/medecins")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public Iterable<Medecin> medecins() throws Exception {
		
		return medrepository.findAll().stream().filter(med->med.getIsActive()==true).collect(Collectors.toList());

	}

//Recuperer touts les medecins de la plateforme en priorisant ceux de Casablanca
	@GetMapping("/allmedecins")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public Iterable<Medecin> allmedecins() throws Exception {
		return medrepository.findAll().stream()
				.filter(Medecin::getIsActive)  // Filtrer seulement les médecins actifs
				.sorted((med1, med2) -> {
					// Trier par priorité à Casablanca
					String ville1 = med1.getVille().getNom_ville();
					String ville2 = med2.getVille().getNom_ville();
					if (ville1.equalsIgnoreCase("Casablanca") && !ville2.equalsIgnoreCase("Casablanca")) {
						return -1; // med1 est avant med2
					} else if (!ville1.equalsIgnoreCase("Casablanca") && ville2.equalsIgnoreCase("Casablanca")) {
						return 1;  // med2 est avant med1
					} else {
						return 0;  // ils sont égaux en priorité
					}
				})
				.collect(Collectors.toList());
	}

	// Get Medecin By Id : %OK%
	/*@GetMapping("/medById/{id}")
	public ResponseEntity<Medecin> getMedecinById(@PathVariable(value="id") Long id) throws Exception {
			
			return ResponseEntity.ok(mapper.map(medecinService.findById(id), Medecin.class));
	}*/

	@GetMapping("/medById/{id}")
	public ResponseEntity<MedecinDTO> getMedecinById(@PathVariable(value="id") Long id) throws Exception {
		Medecin medecin = medecinService.findById(id);
		List<Langue> langues = medecinService.getLanguesByMedecinId(id); // Récupérer les langues du médecin

		medecin.setLangues(langues); // Ajouter les langues au médecin
		List<Tarif>tarifs=medecinService.getTarifByMedecinId(id);
		medecin.setTarifs(tarifs);

		List<Cabinet>cabinetMedecinsSpaces=cabservice.getAllCabinetsByMedecinId(id);


		// Mapping de la la liste de cabinets à une liste de CabinetDTO
		List<CabinetDTO> cabinetDTOList = cabinetMedecinsSpaces.stream()
				.map(cabinet -> mapper.map(cabinet, CabinetDTO.class))
				.collect(Collectors.toList());

		MedecinDTO medecinDTO = mapper.map(medecin, MedecinDTO.class);

		medecinDTO.setCabinet(cabinetDTOList.isEmpty() ? null : cabinetDTOList);

		//medecinDTO.setCabinet(cabinetMedecinsSpaces.isEmpty() ? null : mapper.map(cabinetMedecinsSpaces.get(0), CabinetDTO.class));

		return ResponseEntity.ok(medecinDTO);
	}


	// Get Medecin y his name : %OK%
	/*@GetMapping("/medByName")
	@ResponseBody
	public List<Medecin> findMedByName(@RequestParam String nomMed) throws Exception {


			return medrepository.getMedecinByName(nomMed).stream().filter(med->med.getIsActive()==true).collect(Collectors.toList());
	}*/

	@GetMapping("/medByName")
	@ResponseBody
	public ResponseEntity<List<Medecin>> findMedByName(@RequestParam String nomMed) throws Exception {
		// Récupérer les médecins par leur nom
		List<Medecin> medecins = medrepository.getMedecinByName(nomMed)
				.stream()
				.filter(Medecin::getIsActive) // Filtrer uniquement les médecins actifs
				.collect(Collectors.toList());

		// Pour chaque médecin trouvé, récupérer les langues associées
		for (Medecin medecin : medecins) {
			List<Langue> langues = medecinService.getLanguesByMedecinName(medecin.getNom_med());
			medecin.setLangues(langues);
			List<Tarif>tarifs=medecinService.getTarifByMedecinName(medecin.getNom_med());
			medecin.setTarifs(tarifs);

		}

		return ResponseEntity.ok(medecins);
	}

	// end point for getting Doctorlist by CabinetName: %OK%_
	@GetMapping("/medByCabinetName")
	public ResponseEntity<List<Medecin>> getAllMedecinsByCabinetName(@RequestParam String nomCabinet) {
		try {
			// Appeler la méthode du service pour récupérer les médecins par nom du cabinet
			List<Medecin> medecins = medcabinetservice.getAllMedecinsByCabinetName(nomCabinet);

			// Retourner la liste des médecins dans une réponse HTTP OK
			return ResponseEntity.ok(medecins);
		} catch (RuntimeException e) {
			// En cas d'erreur, retourner une réponse HTTP avec un statut d'erreur et un message d'erreur
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}





	// end point for getting Doctor by Name or cabinet or speciality and city : %OK%____________________________
	/*@GetMapping("/medByNameOrSpecAndVille")
	@ResponseBody
	public Iterable<Medecin> medByNameOrSpecAndVille(@RequestParam String ville,
				@RequestParam String search) throws Exception {

		System.out.println("la ville: "+ ville);
		System.out.println("specialité: "+search);
			return medrepository.getMedecinBySpecialiteOrNameAndVille( search,ville).stream()
			.filter(med->med.getIsActive()==true).collect(Collectors.toList());

	}*/

	@GetMapping("/medByNameOrSpecAndVille")
	@ResponseBody
	public Iterable<Medecin> medByNameOrSpecAndVille(@RequestParam String ville,
													 @RequestParam String search) throws Exception {

		System.out.println("la ville: " + ville);
		System.out.println("recherche: " + search);

		List<Medecin> medecins = medrepository.getMedecinBySpecialiteOrNameOrCabinetAndVille(search, ville).stream()
				.filter(med -> med.getIsActive() == true)
				.collect(Collectors.toList());

		return medecins;
	}
//----------------------------------------------------------------------

	// end point for getting Doctor by Name and speciality : %OK%
	@GetMapping("/medByNameAndSpec")
	public Iterable<Medecin> findMedSpecNameVille(@RequestParam String name,
				@RequestParam String search) throws Exception {
					
		return medrepository.getMedecinBySpecialiteAndName(search, name).stream()
		.filter(med->med.getIsActive()==true).collect(Collectors.toList());
	}

	//Endpoint for getting Doctor by Name or speciality : %OK%---------------------------------

	/*@GetMapping("/medByNameOrSpec")
	public Iterable<Medecin> findMedSpecName(@RequestParam String search) throws Exception {
			
			return medrepository.getMedecinBySpecOrName(search).stream().filter(med->med.getIsActive()==true).collect(Collectors.toList());

	}*/
	@GetMapping("/medByNameOrSpec")
	public Iterable<Medecin> findMedSpecName(@RequestParam String search) throws Exception {

		// Rechercher la spécialité par le nom
		Specialite specialite = specialiteRepository.getSpecialiteByName(search);

		// Vérifier si la recherche correspond à un nom de cabinet
		List<Cabinet> cabinets = cabinetRepository.findByNomContainingIgnoreCase(search);

		if (specialite != null) {

			return medrepository.getMedecinBySpecOrName(search).stream()
					.filter(med -> med.getIsActive())
					.collect(Collectors.toList());
		} else if (!cabinets.isEmpty()) {

			return medcabinetservice.getAllMedecinsByCabinetName(search);
		} else {
			// Sinon, considérer la recherche comme nom d'un médecin
			return medrepository.getMedecinBySpecOrName(search).stream()
					.filter(med -> med.getIsActive())
					.collect(Collectors.toList());
		}
	}
//---------------------------------------------------------------------------




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
	@GetMapping("/getallpatients")
	Iterable <Patient> getallpatients() throws Exception {
			activityServices.createActivity(new Date(), "Read", "Show All Rdv for Medecin",
					globalVariables.getConnectedUser());
			Medecin medecin = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
			List<Long> l=medrepository.findPatientIdsByMedecinId(medecin.getId());
			List<Patient> patients = new ArrayList<>();
			for (Long id : l) {
			patientRepository.findById(id).ifPresent(patients::add);
		}
			LOGGER.info("Show All patients for Medecin, UserID : " + globalVariables.getConnectedUser().getId());

			return patients;
	}




	// Get all medecins ... : %OK%




	// Finding all the schedules bY med Id from a given date.%OK%
	@GetMapping("/schedulesofMed/{idmed}")
	@JsonSerialize(using = LocalDateSerializer.class)  //@ApiParam(value = "startDate", example = "yyyy-MM-dd")
	public List<MedecinSchedule> findallSchudelesfromDate(@PathVariable Long idmed,
														  @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate) throws Exception {
				/*if(globalVariables.getConnectedUser()!=null){
					activityServices.createActivity(new Date(),"Read","Consult Schedules of Medecin by is ID: "+idmed,globalVariables.getConnectedUser());
				LOGGER.info("Consult schedules of Medecin By his ID : "+idmed+" name by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
				}*/
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			// L'utilisateur est authentifié, créez l'activité
			User connectedUser = globalVariables.getConnectedUser();
			if (connectedUser != null) {
				activityServices.createActivity(new Date(),"Read","Consult Medecin Agenda by his ID : "+idmed, connectedUser);
				LOGGER.info("Consult Medecin Agenda By his ID : "+idmed+" by User : "+(connectedUser instanceof User ? connectedUser.getId():""));
			}
		}

		return medScheduleRepo
				.findByMedId(idmed)
				.stream()
				.map(item -> mapper.map(item, MedecinSchedule.class))
				.collect(Collectors.toList());

	}

	// get agenda bY med Id from a given date.%OK%
	/*@GetMapping("/agenda/{idmed}/{weeks}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public List<AgendaResponse> GetCreno(@Validated @PathVariable long idmed, @PathVariable long weeks,
										 @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate)
			throws Exception {

		try {

			List<AgendaResponse> agendaResponseList = new ArrayList<AgendaResponse>();
			//afficher les schedules by week (availability start>= +week*7 and availability_end <=week*7)
			List<MedecinSchedule> schedules = medScheduleRepo
					//.findByMedIdAndStartDateAndWeeksOrderByAvailability(idmed,startDate,weeks)
					.findByMedIdOrderByAvailability(idmed)
					//.findByMedId(idmed)
					.stream()
					.map(item -> mapper.map(item, MedecinSchedule.class))
					.collect(Collectors.toList());

			int days = medecinService.getDaysInMonth(startDate.atStartOfDay());
			//parcours le nbre de semaine en parametre
			for (int j = 1; j <= weeks; j++) {
				//parcours des jours
				for (int i = 1; i <= 7; i++) {
					checkday = false;  // si un jour contient des schedules
					if (!schedules.isEmpty()) { // si la liste des creno de ce medecin pas vide
						for (MedecinSchedule medsch : schedules) { //parcour les creno
							//normalement on doit comparer la date avec la date pas le jour
							if (medsch.getDay().getValue() == startDate.getDayOfWeek().getValue())
									//medsch.getAvailabilityStart().toLocalDate().isAfter(startDate)) // a retirer
							{
							checkday = true;
								AgendaResponse agenda = null;
								// Rechercher une AgendaResponse correspondante dans agendaResponseList
								for (AgendaResponse ag : agendaResponseList) {
									if (ag.getDay().getValue() == medsch.getDay().getValue() && ag.getWeek() == j) {
										agenda = ag;
										break;
									}
								}

								// Si une AgendaResponse correspondante est trouvée, la mettre à jour
								if (agenda != null) {
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.set(agendaResponseList.indexOf(agenda), agenda);
								} else {
									// Sinon, créer une nouvelle AgendaResponse
									agenda = new AgendaResponse();
									agenda.setDay(startDate.getDayOfWeek());
									agenda.setWorkingDate(startDate.atStartOfDay());
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.add(agenda);
								}
								//here


//								for (AgendaResponse ag : agendaResponseList) {
//									if (ag.getDay().getValue() == medsch.getDay().getValue() && ag.getWeek() == j) {
//										int index = agendaResponseList.indexOf(ag);
//										agenda = agendaResponseList.get(index);
//										agenda = medecinService.CreateCreno(medsch, agenda, idmed, j,
//												startDate.atStartOfDay());
//										agendaResponseList.set(index, agenda);
//
//									}
//								}

								//agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());

			 					// diffrance hours :
								long Hours = ChronoUnit.HOURS.between(medsch.getAvailabilityStart(),
										medsch.getAvailabilityEnd());
								agenda.getMedecinTimeTable().add(new GeneralResponse("startTime",
										medsch.getAvailabilityStart()));
								agenda.getMedecinTimeTable().add(new GeneralResponse("endTime",
										medsch.getAvailabilityStart().plusHours(Hours)));
								String startTime = medsch.getAvailabilityStart().getHour() + ":"
										+ medsch.getAvailabilityStart().getMinute();

								String endTime = medsch.getAvailabilityEnd().getHour() + ":"
										+ medsch.getAvailabilityEnd().getMinute();

								agenda.getWorkingHours().add(new HorairesResponse(startTime,
										endTime));

								agendaResponseList.add(agenda);

								continue;

							}

						}
					}
					if (!checkday) {

						AgendaResponse agenda = new AgendaResponse();
						agenda.setDay(startDate.getDayOfWeek());
						agenda.setWorkingDate(startDate.atStartOfDay());
						agendaResponseList.add(agenda);
					}
					startDate = startDate.plusDays(1);//

				}

			}
			// Create a new LinkedHashSet
			Set<AgendaResponse> set = new LinkedHashSet<>();

			// Add the elements to set
			set.addAll(agendaResponseList);

			// Clear the list
			agendaResponseList.clear();

			// add the elements of set
			// with no duplicates to the list
			agendaResponseList.addAll(set);


			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				// L'utilisateur est authentifié, créez l'activité
				User connectedUser = globalVariables.getConnectedUser();
				if (connectedUser != null) {
					activityServices.createActivity(new Date(),"Read","Consult Medecin Agenda by his ID : "+idmed, connectedUser);
					LOGGER.info("Consult Medecin Agenda By his ID : "+idmed+" by User : "+(connectedUser instanceof User ? connectedUser.getId():""));
				}
			}
//			if(globalVariables != null && globalVariables.getConnectedUser()!=null){
//				activityServices.createActivity(new Date(),"Read","Consult Medecin Agenda by his ID : "+idmed,globalVariables.getConnectedUser());
//				LOGGER.info("Consult Medecin Agenda By his ID : "+idmed+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//			}

			return agendaResponseList;

		} catch (Exception e) {
			throw new BadRequestException("error :" + e);
		}

	}*/

	/*@GetMapping("/agenda/{idmed}/{weeks}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public List<AgendaResponse> GetCreno(@Validated @PathVariable long idmed, @PathVariable long weeks,
										 @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate)
			throws Exception {

		try {

			List<AgendaResponse> agendaResponseList = new ArrayList<AgendaResponse>();
			//afficher les schedules by week (availability start>= +week*7 and availability_end <=week*7)
			List<MedecinSchedule> schedules = medScheduleRepo
					//.findByMedIdAndStartDateAndWeeksOrderByAvailability(idmed,startDate,weeks)
					.findByMedIdOrderByAvailability(idmed)
					//.findByMedId(idmed)
					.stream()
					.map(item -> mapper.map(item, MedecinSchedule.class))
					.collect(Collectors.toList());

			int days = medecinService.getDaysInMonth(startDate.atStartOfDay());
			//parcours le nbre de semaine en parametre
			for (int j = 1; j <= weeks; j++) {
				//parcours des jours
				for (int i = 1; i <= 7; i++) {
					checkday = false;  // si un jour contient des schedules
					if (!schedules.isEmpty()) { // si la liste des creno de ce medecin pas vide
						for (MedecinSchedule medsch : schedules) { //parcour les creno
							//normalement on doit comparer la date avec la date pas le jour
							if (medsch.getDay().getValue() == startDate.getDayOfWeek().getValue())
							//medsch.getAvailabilityStart().toLocalDate().isAfter(startDate)) // a retirer
							{
								checkday = true;
								AgendaResponse agenda = null;
								// Rechercher une AgendaResponse correspondante dans agendaResponseList
								for (AgendaResponse ag : agendaResponseList) {
									if (ag.getDay().getValue() == medsch.getDay().getValue() && ag.getWeek() == j) {
										agenda = ag;
										break;
									}
								}

								// Si une AgendaResponse correspondante est trouvée, la mettre à jour
								if (agenda != null) {
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.set(agendaResponseList.indexOf(agenda), agenda);
								} else {
									// Sinon, créer une nouvelle AgendaResponse
									agenda = new AgendaResponse();
									agenda.setDay(startDate.getDayOfWeek());
									agenda.setWorkingDate(startDate.atStartOfDay());
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.add(agenda);
								}
								//here


//                      for (AgendaResponse ag : agendaResponseList) {
//                         if (ag.getDay().getValue() == medsch.getDay().getValue() && ag.getWeek() == j) {
//                            int index = agendaResponseList.indexOf(ag);
//                            agenda = agendaResponseList.get(index);
//                            agenda = medecinService.CreateCreno(medsch, agenda, idmed, j,
//                                  startDate.atStartOfDay());
//                            agendaResponseList.set(index, agenda);
//
//                         }
//                      }

								//agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());

								// diffrance hours :
								long Hours = ChronoUnit.HOURS.between(medsch.getAvailabilityStart(),
										medsch.getAvailabilityEnd());
								agenda.getMedecinTimeTable().add(new GeneralResponse("startTime",
										medsch.getAvailabilityStart()));
								agenda.getMedecinTimeTable().add(new GeneralResponse("endTime",
										medsch.getAvailabilityStart().plusHours(Hours)));
								String startTime = medsch.getAvailabilityStart().getHour() + ":"
										+ medsch.getAvailabilityStart().getMinute();

								String endTime = medsch.getAvailabilityEnd().getHour() + ":"
										+ medsch.getAvailabilityEnd().getMinute();

								agenda.getWorkingHours().add(new HorairesResponse(startTime,
										endTime));

								agendaResponseList.add(agenda);

								continue;

							}

						}
					}
					if (!checkday) {

						AgendaResponse agenda = new AgendaResponse();
						agenda.setDay(startDate.getDayOfWeek());
						agenda.setWorkingDate(startDate.atStartOfDay());
						agendaResponseList.add(agenda);
					}
					startDate = startDate.plusDays(1);//

				}

			}
			// Create a new LinkedHashSet
			Set<AgendaResponse> set = new LinkedHashSet<>();

			// Add the elements to set
			set.addAll(agendaResponseList);

			// Clear the list
			agendaResponseList.clear();

			// add the elements of set
			// with no duplicates to the list
			agendaResponseList.addAll(set);


			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				// L'utilisateur est authentifié, créez l'activité
				User connectedUser = globalVariables.getConnectedUser();
				if (connectedUser != null) {
					activityServices.createActivity(new Date(),"Read","Consult Medecin Agenda by his ID : "+idmed, connectedUser);
					LOGGER.info("Consult Medecin Agenda By his ID : "+idmed+" by User : "+(connectedUser instanceof User ? connectedUser.getId():""));
				}
			}
//       if(globalVariables != null && globalVariables.getConnectedUser()!=null){
//          activityServices.createActivity(new Date(),"Read","Consult Medecin Agenda by his ID : "+idmed,globalVariables.getConnectedUser());
//          LOGGER.info("Consult Medecin Agenda By his ID : "+idmed+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//       }

			return agendaResponseList;

		} catch (Exception e) {
			throw new BadRequestException("error :" + e);
		}

	}*/

	//Recuperation********************************************
	/*@GetMapping("/agenda/{idmed}/{weeks}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public ResponseEntity<?> GetCreno(
			@Validated @PathVariable long idmed,
			@PathVariable long weeks,
			@PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate)
			throws Exception {

		try {
			// Vérifier si le médecin existe
			Medecin medecin = medrepository.findById(idmed)
					.orElseThrow(() -> new BadRequestException("Médecin avec l'ID spécifié n'existe pas."));

			// Vérifier si le médecin a un compte utilisateur
			if (medecin.getUser() == null) {
				return ResponseEntity.ok(Collections.singletonMap("message", "Ce médecin n'est pas encore disponible sur Clinital"));
			}
 // Vérifier si le médecin a des créneaux dans la base de données
        List<MedecinSchedule> rawSchedules = medScheduleRepo.findByMedId(idmed);
        if (rawSchedules.isEmpty()) {
            // Si le médecin n'a pas de créneaux, retourner un message approprié
            return ResponseEntity.ok(Collections.singletonMap("message", "Aucune disponibilité en ligne."));
        }
			List<AgendaResponse> agendaResponseList = new ArrayList<>();
			List<MedecinSchedule> schedules = medScheduleRepo.findByMedIdOrderByAvailability(idmed)
					.stream()
					.map(item -> mapper.map(item, MedecinSchedule.class))
					.collect(Collectors.toList());

			int days = medecinService.getDaysInMonth(startDate.atStartOfDay());

			for (int j = 1; j <= weeks; j++) {
				for (int i = 1; i <= 7; i++) {
					boolean checkday = false;

					if (!schedules.isEmpty()) {
						for (MedecinSchedule medsch : schedules) {
							if (medsch.getDay().getValue() == startDate.getDayOfWeek().getValue()) {
								checkday = true;
								AgendaResponse agenda = null;

								for (AgendaResponse ag : agendaResponseList) {
									if (ag.getDay().getValue() == medsch.getDay().getValue() && ag.getWeek() == j) {
										agenda = ag;
										break;
									}
								}

								if (agenda != null) {
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.set(agendaResponseList.indexOf(agenda), agenda);
								} else {
									agenda = new AgendaResponse();
									agenda.setDay(startDate.getDayOfWeek());
									agenda.setWorkingDate(startDate.atStartOfDay());
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.add(agenda);
								}

								long hours = ChronoUnit.HOURS.between(medsch.getAvailabilityStart(), medsch.getAvailabilityEnd());
								agenda.getMedecinTimeTable().add(new GeneralResponse("startTime", medsch.getAvailabilityStart()));
								agenda.getMedecinTimeTable().add(new GeneralResponse("endTime", medsch.getAvailabilityStart().plusHours(hours)));
								String startTime = medsch.getAvailabilityStart().getHour() + ":" + medsch.getAvailabilityStart().getMinute();
								String endTime = medsch.getAvailabilityEnd().getHour() + ":" + medsch.getAvailabilityEnd().getMinute();
								agenda.getWorkingHours().add(new HorairesResponse(startTime, endTime));

								continue;
							}
						}
					}

					if (!checkday) {
						AgendaResponse agenda = new AgendaResponse();
						agenda.setDay(startDate.getDayOfWeek());
						agenda.setWorkingDate(startDate.atStartOfDay());
						agendaResponseList.add(agenda);
					}
					startDate = startDate.plusDays(1);
				}
			}

			// Supprimer les doublons
			Set<AgendaResponse> set = new LinkedHashSet<>(agendaResponseList);
			agendaResponseList = new ArrayList<>(set);

			// Logging user activity
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				User connectedUser = globalVariables.getConnectedUser();
				if (connectedUser != null) {
					activityServices.createActivity(new Date(), "Read", "Consult Medecin Agenda by his ID : " + idmed, connectedUser);
					LOGGER.info("Consult Medecin Agenda By his ID : " + idmed + " by User : " + (connectedUser instanceof User ? connectedUser.getId() : ""));
				}
			}

			return ResponseEntity.ok(agendaResponseList);

		} catch (Exception e) {
			throw new BadRequestException("error :" + e);
		}
	}*/

	@GetMapping("/agenda/{idmed}/{weeks}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public ResponseEntity<?> GetCreno(
			@Validated @PathVariable long idmed,
			@PathVariable long weeks,
			@PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate)
			throws Exception {

		try {
			// Vérifier si le médecin existe
			Medecin medecin = medrepository.findById(idmed)
					.orElseThrow(() -> new BadRequestException("Médecin avec l'ID spécifié n'existe pas."));

			// Vérifier si le médecin a un compte utilisateur
			if (medecin.getUser() == null) {
				return ResponseEntity.ok(Collections.singletonMap("message", "Ce médecin n'est pas encore disponible sur Clinital"));
			}

			// Vérifier si le médecin a des créneaux dans la base de données
			List<MedecinSchedule> rawSchedules = medScheduleRepo.findByMedId(idmed);
			if (rawSchedules.isEmpty()) {
				return ResponseEntity.ok(Collections.singletonMap("message", "Aucune disponibilité en ligne."));
			}

			List<AgendaResponse> agendaResponseList = new ArrayList<>();
			List<MedecinSchedule> schedules = medScheduleRepo.findByMedIdOrderByAvailability(idmed)
					.stream()
					.map(item -> mapper.map(item, MedecinSchedule.class))
					.collect(Collectors.toList());

			int days = medecinService.getDaysInMonth(startDate.atStartOfDay());

			for (int j = 1; j <= weeks; j++) {
				for (int i = 1; i <= 7; i++) {
					boolean checkday = false;

					if (!schedules.isEmpty()) {
						for (MedecinSchedule medsch : schedules) {
							if (medsch.getDay().getValue() == startDate.getDayOfWeek().getValue()) {
								checkday = true;
								AgendaResponse agenda = null;

								for (AgendaResponse ag : agendaResponseList) {
									if (ag.getDay().getValue() == medsch.getDay().getValue() && ag.getWeek() == j) {
										agenda = ag;
										break;
									}
								}

								if (agenda != null) {
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.set(agendaResponseList.indexOf(agenda), agenda);
								} else {
									agenda = new AgendaResponse();
									agenda.setDay(startDate.getDayOfWeek());
									agenda.setWorkingDate(startDate.atStartOfDay());
									agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
									agendaResponseList.add(agenda);
								}

								long hours = ChronoUnit.HOURS.between(medsch.getAvailabilityStart(), medsch.getAvailabilityEnd());
								agenda.getMedecinTimeTable().add(new GeneralResponse("startTime", medsch.getAvailabilityStart()));
								agenda.getMedecinTimeTable().add(new GeneralResponse("endTime", medsch.getAvailabilityStart().plusHours(hours)));
								String startTime = medsch.getAvailabilityStart().getHour() + ":" + medsch.getAvailabilityStart().getMinute();
								String endTime = medsch.getAvailabilityEnd().getHour() + ":" + medsch.getAvailabilityEnd().getMinute();
								agenda.getWorkingHours().add(new HorairesResponse(startTime, endTime));

								continue;
							}
						}
					}

					if (!checkday) {
						AgendaResponse agenda = new AgendaResponse();
						agenda.setDay(startDate.getDayOfWeek());
						agenda.setWorkingDate(startDate.atStartOfDay());
						agendaResponseList.add(agenda);
					}
					startDate = startDate.plusDays(1);
				}
			}

			// Supprimer les doublons
			Set<AgendaResponse> set = new LinkedHashSet<>(agendaResponseList);
			agendaResponseList = new ArrayList<>(set);

			// Fetch all future appointments for this doctor
			List<Rendezvous> futureAppointments = rdvRepository.findByMedecinIdAndStartAfterOrderByStartAsc(idmed, LocalDateTime.now());

			// Find the next available slot considering both schedules and existing appointments
			Optional<LocalDateTime> nextAvailableSlot = schedules.stream()
					.flatMap(schedule -> {
						LocalDate currentDate = LocalDate.now();
						List<LocalDateTime> slots = new ArrayList<>();

						while (slots.size() < 10) { // Look for the next 10 potential slots
							if (schedule.getDay() == currentDate.getDayOfWeek()) {
								LocalDateTime slotStart = currentDate.atTime(LocalTime.from(schedule.getAvailabilityStart()));
								if (slotStart.isAfter(LocalDateTime.now())) {
									slots.add(slotStart);
								}
							}
							currentDate = currentDate.plusDays(1);
						}
						return slots.stream();
					})
					.filter(slotStart -> {
						// Check if the slot doesn't conflict with existing appointments
						return futureAppointments.stream().noneMatch(appointment ->
								(slotStart.isEqual(appointment.getStart()) || slotStart.isAfter(appointment.getStart()))
										&& slotStart.isBefore(appointment.getEnd())
						);
					})
					.min(Comparator.naturalOrder());

			if (nextAvailableSlot.isPresent()) {
				LocalDateTime nextAvailableDateTime = nextAvailableSlot.get();
				if (nextAvailableDateTime.toLocalDate().isAfter(LocalDate.now().plusWeeks(1))) {
					// Display message for future weeks only
					String formattedDateTime = nextAvailableDateTime.format(DateTimeFormatter.ofPattern("dd MM yyyy "));
					return ResponseEntity.ok(Collections.singletonMap("message", "Prochain RDV le " + formattedDateTime));
				}
			}

			// Logging user activity
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				User connectedUser = globalVariables.getConnectedUser();
				if (connectedUser != null) {
					activityServices.createActivity(new Date(), "Read", "Consult Medecin Agenda by his ID : " + idmed, connectedUser);
					LOGGER.info("Consult Medecin Agenda By his ID : " + idmed + " by User : " + (connectedUser instanceof User ? connectedUser.getId() : ""));
				}
			}
			// If no future slots or all are within the current week
			//return ResponseEntity.ok(Collections.singletonMap("agenda", agendaResponseList));
			return ResponseEntity.ok(agendaResponseList);

		} catch (Exception e) {
			throw new BadRequestException("error :" + e);
		}
	}




	private boolean isConflicting(AgendaResponse existingAgenda, MedecinSchedule newSchedule) {
	LocalTime existingStart = existingAgenda.getWorkingDate().toLocalTime();
	LocalTime existingEnd = existingAgenda.getWorkingDate().plusDays(1).toLocalTime(); // Fin de la journée de travail

	LocalTime newStart = newSchedule.getAvailabilityStart().toLocalTime();
	LocalTime newEnd = newSchedule.getAvailabilityEnd().toLocalTime();

	// Vérifiez les conflits en fonction des heures
	return (newStart.isAfter(existingStart) && newStart.isBefore(existingEnd)) ||
			(newEnd.isAfter(existingStart) && newEnd.isBefore(existingEnd)) ||
			(newStart.isBefore(existingStart) && newEnd.isAfter(existingEnd)) ||
			(newStart.equals(existingStart) || newEnd.equals(existingEnd));
}


	// Finding all the RDV bY med Id from a given date.%OK%
	@GetMapping("/rdvofMed/{idmed}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public ResponseEntity<?> findallRDVforMedBystartDate(@PathVariable Long idmed,
																	 @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate) throws Exception {

		LocalDateTime startDateTime = startDate.atStartOfDay();
		if(globalVariables.getConnectedUser()!=null){
			activityServices.createActivity(new Date(),"Read","Consult Medecin RDV By his ID : "+idmed,globalVariables.getConnectedUser());
			LOGGER.info("Consult Medecin RDV By his ID : "+idmed+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		}

		return ResponseEntity.ok(rendezvousService
				.findRendezvousByMedAndDate(idmed, startDateTime)
				.stream().map(rdv -> mapper.map(rdv, RendezvousDTO.class))
				.collect(Collectors.toList()));

	}

	//FILTRE DE MEDECIN SELON LA DISPONIBILITÉ-------------------------------------------

	/*@PostMapping("/medecins/schedules/filter")
	public ResponseEntity<?> filterMedecinSchedulesByAvailability(
			@RequestBody FilterRequest filterRequest
	) {
		List<Long> medecinIds = filterRequest.getMedecinIds();
		String filter = filterRequest.getFilter();
		System.out.println("filtre:" + filter);
		System.out.println("Les ids de medecins: " + medecinIds);

		// Utilisation de medecinIds et filter pour filtrer les médecins
		List<Medecin> filteredMedecins = medecinScheduleService.filterMedecinsByAvailability(medecinIds, filter);

		// Vérifiez si des médecins ont été trouvés
		if (filteredMedecins.isEmpty()) {

			System.out.println( "Aucun médecin trouvé avec ce creneau.");
			return ResponseEntity.ok(new ApiResponse(false, "Aucun medecin trouvé avec ce creneau."));

		}
		System.out.println("Medecins trouvés"+filteredMedecins.size());
		return ResponseEntity.ok(filteredMedecins);

	}*/
	@PostMapping("/medecins/schedules/filter")
	public ResponseEntity<?> filterMedecinSchedulesByAvailability(
			@RequestBody FilterRequest filterRequest
	) {
		List<Long> medecinIds = filterRequest.getMedecinIds();
		List<String> filters = filterRequest.getFilters(); // Récupérez la liste de filtres
		System.out.println("filtres:" + filters);
		System.out.println("Les ids de medecins: " + medecinIds);

		// Utilisation de medecinIds et filters pour filtrer les médecins
		List<Medecin> filteredMedecins = medecinScheduleService.filterMedecinsByAvailability(medecinIds, filters);

		// Vérifiez si des médecins ont été trouvés
		if (filteredMedecins.isEmpty()) {
			System.out.println("Aucun médecin trouvé avec ces créneaux.");
			return ResponseEntity.ok(new ApiResponse(false, "Aucun médecin trouvé avec ces créneaux."));
		}

		System.out.println("Médecins trouvés: " + filteredMedecins.size());
		return ResponseEntity.ok(filteredMedecins);
	}


	//FILTRE LE MEDECIN PAR LANGUE test
	/*@GetMapping("/byLangue/{langueName}")
	public ResponseEntity<List<Medecin>> getMedecinsByLangueName(@PathVariable String langueName) {
		try {
			List<Medecin> medecins = medecinService.findMedecinsByLangues_Name(langueName);
			return ResponseEntity.ok(medecins);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}*/

	// FILTRE LE MEDECIN PAR LANGUE OK
/*	@PostMapping("/byLangue")
	public ResponseEntity<?> getMedecinsByLangue(@RequestBody FilterRequest filterRequest) {
		try {
			List<Long> medecinIds = filterRequest.getMedecinIds();
			String filter = filterRequest.getFilter();
			System.out.println("filtre:" + filter);
			System.out.println("Les ids de medecins: " + medecinIds);
			// Utilisez le service pour filtrer les médecins par langue
			List<Medecin> medecins = medecinService.filterMedecinsByLangue(filterRequest.getMedecinIds(), filterRequest.getFilter());

			// Vérifiez si des médecins ont été trouvés
			if (medecins.isEmpty()) {
				System.out.println("Aucun médecin trouvé parlant cette langue.");
				return ResponseEntity.ok(new ApiResponse(false, "Aucun médecin trouvé parlant cette langue."));
			}
			System.out.println("Medecins trouvés :"+medecins.size());
			return ResponseEntity.ok(medecins);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}*/
	@PostMapping("/byLangue")
	public ResponseEntity<?> getMedecinsByLangue(@RequestBody FilterRequest filterRequest) {
		try {
			List<Long> medecinIds = filterRequest.getMedecinIds();
			List<String> filters = filterRequest.getFilters(); // Récupérez la liste de langues

			System.out.println("Filtres (langues) : " + filters);
			System.out.println("Les IDs de médecins : " + medecinIds);

			// Utilisez le service pour filtrer les médecins par langues
			List<Medecin> medecins = medecinService.filterMedecinsByLangue(medecinIds, filters);

			// Vérifiez si des médecins ont été trouvés
			if (medecins.isEmpty()) {
				System.out.println("Aucun médecin trouvé parlant ces langues.");
				return ResponseEntity.ok(new ApiResponse(false, "Aucun médecin trouvé parlant ces langues."));
			}

			System.out.println("Médecins trouvés : " + medecins.size());
			return ResponseEntity.ok(medecins);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/equipe")
	public EquipeDTO equipeForMedecin() throws Exception {
		Medecin medecin = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		Long idcab = medecin.getFirstCabinetId();

		List<Medecin> medecins = medrepository.getAllMedecinsByCabinetId(idcab);
		List<Secretaire> secretaires = secretaireService.findByIdCabinet(idcab);
		List<Assistant> assistants = assistantService.findByIdCabinet(idcab);
		EquipeDTO equipe = new EquipeDTO();
		equipe.setMedecins(medecins);
		equipe.setSecretaires(secretaires);
		equipe.setAssistants(assistants);

//    activityServices.createActivity(new Date(), "Read", "Show Equipe for Medecin ",
//            globalVariables.getConnectedUser());
	    LOGGER.info("Show equipe medecin");
		return equipe;
	}

	//-----------------------------------------------NETWORK---------------------------------------------------
	// Add a New Doctor to the Network : %OK%
	@PostMapping("/addNewNetwork")
	public ResponseEntity<?> addNewNetwork(@Valid @RequestBody networkRequest network) throws Exception {
		Long followerId = network.getFollower_id();
		Medecin follower = medrepository.getMedecinById(network.getFollower_id());

        //Verification du medecin liés au user connecter
		Medecin connectedMedecin = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		if (connectedMedecin == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de trouver le médecin connecté.");
		}
		//Verification du medecin qu'on veut follow
		if (follower == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le médecin follower spécifié n'existe pas.");
		}
		// Vérifier si le médecin follower est le même que le médecin connecté
		if (follower.getId().equals(connectedMedecin.getId())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous ne pouvez pas vous suivre vous-même.");
		}
		Medecin followers = medrepository.findFollowerInNetwork(connectedMedecin.getId(), followerId);
		if (followers != null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Le médecin que vous essayez d'ajouter est déjà dans votre réseau.");
		}


		MedecinNetwork medNet = medecinNetworkService.addMedecinNetwork(network, globalVariables.getConnectedUser().getId());
		activityServices.createActivity(new Date(), "Add", "Add Medecin By ID: " + network.getFollower_id() + " for Connected Medecin Network", globalVariables.getConnectedUser());
		LOGGER.info("Add Medecin by id " + network.getFollower_id() + " for Medecin Connected, User ID: " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
		return ResponseEntity.ok(mapper.map(medNet, MedecinNetwork.class));
	}

	@GetMapping("/checkIfInNetwork/{followerId}")
	public ResponseEntity<?> checkIfInNetwork(@PathVariable Long followerId) {
		try {
			Medecin connectedMedecin = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());

			// Logique pour vérifier si le médecin est dans le réseau
			Medecin followers = medrepository.findFollowerInNetwork(connectedMedecin.getId(), followerId);
			if (followers != null) {
				return ResponseEntity.ok("true");
			} else {
				return ResponseEntity.ok("false");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la vérification du médecin dans le réseau.");
		}
	}
	@GetMapping("/getMedNetWork/{follower_id}")
	public ResponseEntity<?> getMedecinNetworkbyId(@Valid @PathVariable Long follower_id) throws Exception {
		// Récupérer le médecin follower à partir de l'ID follower
		Medecin follower = medrepository.getMedecinById(follower_id);
		if (follower == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le médecin follower spécifié n'existe pas.");
		}
		List<Cabinet> cabinets = cabservice.getAllCabinetsByMedecinId(follower_id);

		// Récupérer le médecin connecté à partir de l'ID utilisateur
		Medecin med = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());

		// Créer une activité de consultation
		activityServices.createActivity(new Date(), "Read", "Consulting Medecin Follower By ID: " + follower_id + " for Connected Medecin Network", globalVariables.getConnectedUser());
		LOGGER.info("Consulting Medecin Follower by id " + follower_id + " for Medecin Connected, User ID: " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));

		// Mapper et retourner le DTO du médecin follower
		MedecinDTO medecinDTO = mapper.map(follower, MedecinDTO.class);
		List<CabinetDTO> cabinetDTOList = cabinets.stream()
				.map(cabinet -> mapper.map(cabinet, CabinetDTO.class))
				.collect(Collectors.toList());

		// Assigner la liste de cabinets au MedecinDTO
		medecinDTO.setCabinet(cabinetDTOList);
		return ResponseEntity.ok(medecinDTO);
	}

	// Delete a Medecin from network : %OK%
	@DeleteMapping(path = "/deleteNetwork/{follower_id}")
	public ResponseEntity<?> deleteMedecinNetwork(@Valid @PathVariable Long follower_id) throws Exception {

		Medecin med = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		Medecin follower = medrepository.getMedecinById(follower_id);
		if (follower == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le médecin follower spécifié n'existe pas.");
		}

		medecinNetworkService.deleteMedecinNetwork(med.getId(), follower_id);

		return ResponseEntity.ok(new ApiResponse(true, "Deleted"));

	}


	// Show all Network of a doc : %OK%
	@GetMapping("/getAllMedNetWork")
	public ResponseEntity<List<MedecinDTO>> getAllMedecinNetwork() throws Exception {
		Medecin med = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		List<MedecinDTO> followers = medecinNetworkService.getAllMedecinNetwork(med.getId()).stream()
				.map(follower -> mapper.map(follower, MedecinDTO.class))
				.collect(Collectors.toList());
		activityServices.createActivity(new Date(), "Read", "Consult Medecin Network for Connected Medecin Network", globalVariables.getConnectedUser());
		LOGGER.info("Consult Medecin Network for Medecin Connected, User ID: " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
		return ResponseEntity.ok(followers);
	}

	// Update an existing Doctor in the Network
	@PutMapping("/updateNetwork")
	public ResponseEntity<?> updateNetwork(@Valid @RequestBody MedecinNetworkDTO medecinNetworkDTO) throws Exception {
		MedecinNetworkDTO updatedMedNet = medecinNetworkService.updateMedecinNetwork(medecinNetworkDTO);
		activityServices.createActivity(new Date(), "Update", "Updated Medecin Network for ID: " + medecinNetworkDTO.getId().getId_medecin() + " with follower ID: " + medecinNetworkDTO.getId().getId_follower(), globalVariables.getConnectedUser());
		LOGGER.info("Updated Medecin Network for ID: " + medecinNetworkDTO.getId().getId_medecin() + " with follower ID: " + medecinNetworkDTO.getId().getId_follower() + ", User ID: " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
		return ResponseEntity.ok(updatedMedNet);
	}



	//---FILTRE MEDECIN NETWORK----------------------------------
	//FILTRE MULTI VILLE
	@GetMapping("/medNetByVille")
	public ResponseEntity<List<MedecinDTO>> findMedecinnetByVille(@RequestParam List<Long> id_ville) throws Exception {
		// Récupérer le médecin connecté (supposons que cela soit déjà fait)
		Medecin medecinConnecte = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());

		// Récupérer les médecins du réseau (les followers) du médecin connecté
		List<MedecinDTO> followers = medecinNetworkService.getAllMedecinNetwork(medecinConnecte.getId()).stream()
				.map(follower -> mapper.map(follower, MedecinDTO.class))
				.collect(Collectors.toList());

		// Filtrer les followers par ID de ville
		List<MedecinDTO> medecinsFiltres = followers.stream()
				.filter(follower -> {
					// Vérifier si l'ID de ville du follower est contenu dans la liste des IDs de ville fournie
					Long idVilleFollower = follower.getVille() != null ? follower.getVille().getId_ville() : null;
					return idVilleFollower != null && id_ville.contains(idVilleFollower);
				})
				.collect(Collectors.toList());

		// Enregistrer l'activité
		LOGGER.info("Consult Medecin Network by Ville for Connected Medecin Network, User ID: " + globalVariables.getConnectedUser().getId());

		return ResponseEntity.ok(medecinsFiltres);
	}
/*@GetMapping("/medNetByVille")
	public ResponseEntity<List<MedecinDTO>> findMedNetByVille(@RequestParam Long id_ville) throws Exception {
		// Récupérer l'utilisateur connecté
		User connectedUser = globalVariables.getConnectedUser();

		// Récupérer le médecin correspondant à l'utilisateur connecté
		Medecin medecin = medrepository.getMedecinByUserId(connectedUser.getId());

		// Récupérer les followers du réseau de l'utilisateur connecté et les convertir en DTO
		List<MedecinDTO> followers = medecinNetworkService.getAllMedecinNetwork(medecin.getId()).stream()
				.map(follower -> mapper.map(follower, MedecinDTO.class))
				.collect(Collectors.toList());

		// Filtrer les followers par ville
		List<MedecinDTO> medecinsByVille = followers.stream()
				.filter(follower -> follower.getVille() != null && follower.getVille().getId_ville().equals(id_ville))
				.collect(Collectors.toList());

		// Enregistrer l'activité
		//activityServices.createActivity(new Date(), "Read", "Consult Medecin Network by Ville", connectedUser);
		LOGGER.info("Consult Medecin Network by Ville for Connected Medecin, User ID: " + connectedUser.getId());

		return ResponseEntity.ok(medecinsByVille);
	}*/

	//BYSPECIALITY
	//Filtre multispecialité
	@GetMapping("/medNetByNameOrSpec")
	public ResponseEntity<List<MedecinDTO>> findMedecinnetBySpecialiteOrName(@RequestParam String search) throws Exception {
		// Récupérer le médecin connecté (supposons que cela soit déjà fait)
		Medecin medecinConnecte = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());

		// Récupérer les médecins du réseau (les followers) du médecin connecté
		List<MedecinDTO> followers = medecinNetworkService.getAllMedecinNetwork(medecinConnecte.getId()).stream()
				.map(follower -> mapper.map(follower, MedecinDTO.class))
				.collect(Collectors.toList());

		// Split the search parameter into a list of specialities or names
		List<String> searchTerms = Arrays.asList(search.split(","));

		// Filtrer les followers par spécialité ou nom
		List<MedecinDTO> medecinsFiltres = followers.stream()
				.filter(follower -> {
					// Vérifier si la spécialité ou le nom de follower correspond à l'une des recherches
					Optional<SpecialiteDTO> specialiteFollower = Optional.ofNullable(follower.getSpecialite());
					boolean matchesSpecialite = specialiteFollower.isPresent() &&
							searchTerms.contains(specialiteFollower.get().getLibelle());

					String nomFollower = follower.getNom_med();
					boolean matchesName = nomFollower != null && searchTerms.contains(nomFollower);

					return matchesSpecialite || matchesName;
				})
				.collect(Collectors.toList());

		// Enregistrer l'activité
		LOGGER.info("Consult Medecin Network by Specialities or Name for Connected Medecin Network, User ID: " + globalVariables.getConnectedUser().getId());

		return ResponseEntity.ok(medecinsFiltres);
	}

	/*@GetMapping("/medNetByNameOrSpecAndVille")
	@ResponseBody
	public ResponseEntity<List<MedecinDTO>> medNetByNameOrSpecAndVille(@RequestParam String ville,
																	   @RequestParam String search) throws Exception {

		System.out.println("la ville: " + ville);
		System.out.println("recherche: " + search);

		// Récupérer le médecin connecté (supposons que cela soit déjà fait)
		Medecin medecinConnecte = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());

		// Récupérer les médecins du réseau (les followers) du médecin connecté
		List<MedecinDTO> followers = medecinNetworkService.getAllMedecinNetwork(medecinConnecte.getId()).stream()
				.map(follower -> mapper.map(follower, MedecinDTO.class))
				.collect(Collectors.toList());

		// Filtrer les médecins par spécialité ou nom et par ville si spécifié
		List<MedecinDTO> medecinsFiltres = followers.stream()
				.filter(follower -> {
					// Vérifier si la spécialité ou le nom correspond
					Optional<SpecialiteDTO> specialiteFollower = Optional.ofNullable(follower.getSpecialite());
					String nomFollower = follower.getNom_med();

					boolean matchesSpecialiteOrName = specialiteFollower.isPresent() && search.contains(specialiteFollower.get().getLibelle())
							|| nomFollower != null && search.contains(nomFollower);

					// Vérifier si la ville correspond si spécifiée
					boolean matchesVille = true;
					if (ville != null && !ville.isEmpty()) {
						matchesVille = ville.equalsIgnoreCase(Optional.ofNullable(follower.getVille())
								.map(VilleDTO::getNom_ville)
								.orElse(""));
					}

					return matchesSpecialiteOrName && matchesVille;
				})
				.collect(Collectors.toList());

		// Enregistrer l'activité
		LOGGER.info("Consult Medecin Network by Specialities, Name and City for Connected Medecin Network, User ID: " + globalVariables.getConnectedUser().getId());

		return ResponseEntity.ok(medecinsFiltres);
	}*/


	@GetMapping("/medNetByNameOrSpecAndVille")
	public ResponseEntity<List<MedecinDTO>> medNetByNameOrSpecAndVille(
			@RequestParam(name = "ville") List<Long> id_villes,
			@RequestParam(name = "search") List<String> searchTerms) throws Exception {

		// Vérification des paramètres
		if (id_villes.isEmpty() && searchTerms.isEmpty()) {
			// Si aucun paramètre n'est spécifié, renvoyer une liste vide ou un message d'erreur approprié
			return ResponseEntity.badRequest().build();
		}

		// Récupérer le médecin connecté
		Medecin medecinConnecte = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());

		// Récupérer les médecins du réseau (les followers) du médecin connecté
		List<MedecinDTO> followers = medecinNetworkService.getAllMedecinNetwork(medecinConnecte.getId()).stream()
				.map(follower -> mapper.map(follower, MedecinDTO.class))
				.collect(Collectors.toList());

		// Filtrer les médecins par spécialité ou nom et par ID de ville spécifié
		List<MedecinDTO> medecinsFiltres = followers.stream()
				.filter(follower -> {
					boolean matchesSpecialiteOrName = false;
					boolean matchesVilles = false;

					// Vérifier si la spécialité ou le nom correspond
					Optional<SpecialiteDTO> specialiteFollower = Optional.ofNullable(follower.getSpecialite());
					String nomFollower = follower.getNom_med();

					if (!searchTerms.isEmpty()) {
						matchesSpecialiteOrName = specialiteFollower.isPresent() && searchTerms.stream().anyMatch(term -> term.contains(specialiteFollower.get().getLibelle()))
								|| nomFollower != null && searchTerms.stream().anyMatch(term -> term.contains(nomFollower));
					}

					// Vérifier si l'ID de la ville correspond si spécifié
					if (!id_villes.isEmpty()) {
						matchesVilles = id_villes.contains(follower.getVille().getId_ville());
					}

					return matchesSpecialiteOrName && matchesVilles;
				})
				.collect(Collectors.toList());

		// Enregistrer l'activité
		LOGGER.info("Consult Medecin Network by Specialities, Name and City ID for Connected Medecin Network, User ID: " + globalVariables.getConnectedUser().getId());

		return ResponseEntity.ok(medecinsFiltres);
	}


//------------------------------------------------------------------------------------------------------------------
@GetMapping("/medByLetter")
@ResponseBody
public ResponseEntity<List<Medecin>> findMedByLetter(@RequestParam String lettre) throws Exception {
	// Valider la lettre
	if (lettre == null || lettre.length() != 1) {
		return ResponseEntity.badRequest().body(null);
	}

	// Récupérer les médecins dont le nom commence par la lettre spécifiée
	List<Medecin> medecins = medrepository.getMedecinByNameStartingWith(lettre.toUpperCase())
			.stream()
			.filter(Medecin::getIsActive) // Filtrer uniquement les médecins actifs
			.collect(Collectors.toList());

	// Pour chaque médecin trouvé, récupérer les langues et tarifs associés
	for (Medecin medecin : medecins) {
		List<Langue> langues = medecinService.getLanguesByMedecinName(medecin.getNom_med());
		medecin.setLangues(langues);
		List<Tarif> tarifs = medecinService.getTarifByMedecinName(medecin.getNom_med());
		medecin.setTarifs(tarifs);
	}

	return ResponseEntity.ok(medecins);
}

//RECHERCHE DE MEDECIN PAR MOTIF

	@PostMapping("/by_motif_consultation")
	public ResponseEntity<List<Medecin>> getMedecinsByMotif(@RequestBody MotifRequest motifRequest) {
		try {
			List<String> libellesMotifs = motifRequest.getLibellesMotifs();
			List<Long> medecinIds = motifRequest.getMedecinIds(); //recuperation des iDS DES MEDECINS

			// Appeler le service pour obtenir les médecins filtrés par motifs
			List<Medecin> medecins = medecinService.getMedecinsByMotif(libellesMotifs, medecinIds);

			// Vérifiez si des médecins ont été trouvés
			if (medecins.isEmpty()) {
				return ResponseEntity.ok(new ArrayList<Medecin>());
			}

			return ResponseEntity.ok(medecins);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

//FILTRE COMBINE------------------------------------

	@PostMapping("/combinedfilter")
	public ResponseEntity<List<Medecin>> filterMedecinsCombined(
			@RequestBody MedecinFilterRequest filterRequest) {
		try {
			LOGGER.info("Received filter request: {}", filterRequest);
			// Filtrer les médecins en utilisant le service approprié
			// Affichage des filtres choisis
			LOGGER.info("Selected availability filters: {}", filterRequest.getAvailabilityFilters());
			LOGGER.info("Selected language filters: {}", filterRequest.getLangueFilters());
			LOGGER.info("Selected motives filters: {}", filterRequest.getMotifs());

			List<Medecin> filteredMedecins = medecinFilterService.filterMedecins(
					filterRequest.getMedecinIds(),
					filterRequest.getLangueFilters(),
					filterRequest.getMotifs(),
					filterRequest.getAvailabilityFilters()
			);
			LOGGER.info("Number of filtered doctors: {}", filteredMedecins.size());
			// Vérifiez si des médecins ont été trouvés
			if (filteredMedecins.isEmpty()) {
				LOGGER.info("No doctors found matching the filters.");
				// Retourne une réponse avec une liste vide si aucun médecin n'est trouvé
				return ResponseEntity.ok(new ArrayList<Medecin>());
			}

			// Retourne la liste des médecins filtrés
			return ResponseEntity.ok(filteredMedecins);

		} catch (Exception e) {
			// Retourne une réponse avec un code d'erreur interne du serveur en cas d'exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}



}
