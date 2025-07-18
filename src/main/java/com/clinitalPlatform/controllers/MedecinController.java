package com.clinitalPlatform.controllers;


import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.clinitalPlatform.dto.*;
import com.clinitalPlatform.enums.ConsultationPeriodEnum;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.exception.ResourceNotFoundException;
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

import javassist.NotFoundException;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.util.PDFGenerator;
import com.clinitalPlatform.services.interfaces.SpecialiteService;
import com.clinitalPlatform.models.User;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

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

	@Autowired
	private VirementBancaireRepository virementBancaireRepository;

	@Autowired
	private SpecialiteRechercheService specialiteRechercheService;

	@Autowired
	private FermetureExceptionnelleRepository fermetureRepo;


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

    @GetMapping("/allcabinet")
    public ResponseEntity<?> getAllCabinets() {
        try {
            LOGGER.info("Get all cabinet from db start");

            List<Cabinet> cabinets = cabinetRepository.findAll();
            LOGGER.info("Get all cabinet from db");
            if (cabinets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Aucun cabinet trouvé.");
            }
            return ResponseEntity.ok(cabinets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne du serveur : " + e.getMessage());
        }
    }

	// Get Medecin By Id : %OK%
	/*@GetMapping("/medById/{id}")
	public ResponseEntity<Medecin> getMedecinById(@PathVariable(value="id") Long id) throws Exception {
			
			return ResponseEntity.ok(mapper.map(medecinService.findById(id), Medecin.class));
	}*/

	@GetMapping("/medById/{id}")
	public ResponseEntity<MedecinDTO> getMedecinById(@PathVariable(value="id") Long id) throws Exception {
		// Récupérer le médecin par son ID
		Medecin medecin = medecinService.findById(id);

		// Récupérer les langues du médecin
		List<Langue> langues = medecinService.getLanguesByMedecinId(id);
		medecin.setLangues(langues);

		// Récupérer les tarifs du médecin
		List<Tarif> tarifs = medecinService.getTarifByMedecinId(id);
		medecin.setTarifs(tarifs);

		// Récupérer les cabinets du médecin
		List<Cabinet> cabinetMedecinsSpaces = cabservice.getAllCabinetsByMedecinId(id);
		List<CabinetDTO> cabinetDTOList = cabinetMedecinsSpaces.stream()
				.map(cabinet -> mapper.map(cabinet, CabinetDTO.class))
				.collect(Collectors.toList());

		// Récupérer les moyens de paiement du médecin et les mapper
		List<MoyenPaiement> moyenPaiements = new ArrayList<>();
		for (MoyenPaiement moyen : medecin.getMoyenPaiement()) {
			if (!moyenPaiements.contains(moyen)) {
				moyenPaiements.add(moyen);
			}
		}
		List<MoyenPaiementDTO> moyenPaiementDTOList = moyenPaiements.stream()
				.map(moyen -> {
					MoyenPaiementDTO moyenPaiementDTO = mapper.map(moyen, MoyenPaiementDTO.class);
					moyenPaiementDTO.setType(moyen.getType());
					return moyenPaiementDTO;
				})
				.collect(Collectors.toList());

		// Mapper le médecin en DTO
		MedecinDTO medecinDTO = mapper.map(medecin, MedecinDTO.class);

		// Ajouter les cabinets et moyens de paiement au DTO
		medecinDTO.setCabinet(cabinetDTOList.isEmpty() ? null : cabinetDTOList);
		medecinDTO.setMoyenPaiement(moyenPaiementDTOList.isEmpty() ? null : moyenPaiementDTOList);

		return ResponseEntity.ok(medecinDTO);
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMedecinByConnectedUser() {
		try {
			// Récupérer l'utilisateur connecté
			User connectedUser = globalVariables.getConnectedUser();
			if (connectedUser == null) {
				return ResponseEntity.badRequest().body("User not connected.");
			}

			// Récupérer le médecin associé à l'utilisateur
			Medecin medecin = medecinService.getMedecinByUserId(connectedUser.getId());
			if (medecin == null) {
				return ResponseEntity.status(404).body("Médecin non trouvé pour cet utilisateur.");
			}

			// Récupérer les langues du médecin
			List<Langue> langues = medecinService.getLanguesByMedecinId(medecin.getId());
			medecin.setLangues(langues);

			// Récupérer les tarifs du médecin
			List<Tarif> tarifs = medecinService.getTarifByMedecinId(medecin.getId());
			medecin.setTarifs(tarifs);

			// Récupérer les cabinets du médecin et les mapper
			List<Cabinet> cabinets = cabservice.getAllCabinetsByMedecinId(medecin.getId());
			List<CabinetDTO> cabinetDTOList = cabinets.stream()
					.map(cabinet -> mapper.map(cabinet, CabinetDTO.class))
					.collect(Collectors.toList());

			// Récupérer et mapper les moyens de paiement
			List<MoyenPaiementDTO> moyenPaiementDTOList = medecin.getMoyenPaiement().stream()
					.distinct()
					.map(moyen -> {
						MoyenPaiementDTO moyenPaiementDTO = mapper.map(moyen, MoyenPaiementDTO.class);
						moyenPaiementDTO.setType(moyen.getType());
						return moyenPaiementDTO;
					})
					.collect(Collectors.toList());

			// Mapper le médecin en DTO
			MedecinDTO medecinDTO = mapper.map(medecin, MedecinDTO.class);

			// Ajouter les cabinets et moyens de paiement au DTO
			medecinDTO.setCabinet(cabinetDTOList.isEmpty() ? null : cabinetDTOList);
			medecinDTO.setMoyenPaiement(moyenPaiementDTOList.isEmpty() ? null : moyenPaiementDTOList);

			return ResponseEntity.ok(medecinDTO);

		} catch (NotFoundException e) {
			return ResponseEntity.status(404).body("{\"error\": \"" + e.getMessage() + "\"}");
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(404).body("{\"error\": \"" + e.getMessage() + "\"}");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("{\"error\": \"Une erreur interne est survenue.\"}");
		}
	}

	@PutMapping("/updateMed")
	public ResponseEntity<?> updateMedecin(@RequestBody MedecinRequest medecinDTO) {
		try {
			// Récupérer l'utilisateur connecté
			User connectedUser = globalVariables.getConnectedUser();
			if (connectedUser == null) {
				return ResponseEntity.badRequest().body("User not connected.");
			}

			// Récupérer le médecin associé à l'utilisateur
			Medecin medecin = medecinService.getMedecinByUserId(connectedUser.getId());
			if (medecin == null) {
				return ResponseEntity.status(404).body("Médecin non trouvé pour cet utilisateur.");
			}
            LOGGER.info("Description : {}", medecinDTO);
			// Mettre à jour le médecin
			medecinService.updateMedecin(medecin , medecinDTO);

			return ResponseEntity.ok(new ApiResponse(true, "Médecin mis à jour avec succès."));

		} catch (Exception e) {
			return ResponseEntity.status(500).body("{\"error\": \"Une erreur interne est survenue.\"}");
		}
	}

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

		// Récupérer les médecins correspondant à la recherche
		List<Medecin> medecins = medrepository.getMedecinBySpecialiteOrNameOrCabinetAndVille(search, ville)
				.stream()
				.filter(med -> med.getIsActive())
				.collect(Collectors.toList());

		// Vérifier si la recherche concerne une spécialité et l’incrémenter dans les stats
		Specialite specialite = specialiteRepository.getSpecialiteByName(search);
		if(specialite != null) {
			specialiteRechercheService.incrementerRecherche(specialite.getId_spec());
		}

		return medecins;
	}
//----------------------------------------------------------------------

	// end point for getting Doctor by Name and speciality : %OK%
	@GetMapping("/medByNameAndSpec")
	public Iterable<Medecin> findMedSpecNameVille(@RequestParam String name,
				@RequestParam String search) throws Exception {

		List<Medecin> medecins = medrepository.getMedecinBySpecialiteAndName(search, name)
				.stream()
				.filter(med -> med.getIsActive() == true)
				.collect(Collectors.toList());

		// Vérifier si la spécialité existe et incrémenter son compteur
		Specialite specialite = specialiteRepository.getSpecialiteByName(search);
		if(specialite != null) {
			specialiteRechercheService.incrementerRecherche(specialite.getId_spec());
		}
		return medecins;
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
			specialiteRechercheService.incrementerRecherche(specialite.getId_spec());
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
	public ResponseEntity<?> findMedByVille(@RequestParam(required = true) Long id_ville) {
		try {
			if (id_ville == null || id_ville <= 0) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "ID de ville invalide."));
			}

			List<Medecin> medecinsActifs = medrepository.getMedecinByVille(id_ville)
					.stream()
					.filter(Medecin::getIsActive)
					.collect(Collectors.toList());

			return ResponseEntity.ok(medecinsActifs);

		} catch (Exception e) {
			log.error("Erreur lors de la récupération des médecins pour la ville {} : {}", id_ville, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Erreur interne du serveur."));
		}
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

	@PutMapping("/updatecabinet/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public ResponseEntity<?> UpdateCabinet(@Valid @RequestBody CabinetRequest cabinetreq, @PathVariable long id) throws Exception {

		Medecin med = medecinService.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		Optional<CabinetMedecinsSpace> isAdminMed = cabmedrep.isAdmin(med.getId(), id);

		if (isAdminMed.isPresent()) {
			cabservice.updateCabinet(cabinetreq , id);
			activityServices.createActivity(new Date(),"Update","Update Cabinet ID: "+ id +" By Connected Medecin Admin",globalVariables.getConnectedUser());
			return ResponseEntity.ok("Cabinet has been updated successefully");
		} else
			throw new BadRequestException("You are Not Allowed");

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
	        DocumentsCabinet savedDoc = docservices.create(documentReq , med);
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
	public ResponseEntity<?> getAllPatients() {
		try {
			Long userId = globalVariables.getConnectedUser().getId();
			Medecin medecin = medrepository.getMedecinByUserId(userId);

			if (medecin == null) {
				LOGGER.warn("Aucun médecin trouvé pour l'utilisateur ID: " + userId);
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("Médecin introuvable pour l'utilisateur connecté.");
			}

			List<Long> patientIds = medrepository.findPatientIdsByMedecinId(medecin.getId());
			List<Patient> patients = new ArrayList<>();

			for (Long id : patientIds) {
				patientRepository.findById(id).ifPresent(patients::add);
			}

			activityServices.createActivity(new Date(), "Read", "Show All Patients for Medecin", globalVariables.getConnectedUser());
			LOGGER.info("Récupération de tous les patients pour le médecin. UserID: " + userId);

			return ResponseEntity.ok(patients);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de la récupération des patients : ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Une erreur est survenue lors de la récupération des patients.");
		}
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

	//AFFICHAGE DE L'AGENDA
	@GetMapping("/agenda/{idmed}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public ResponseEntity<?> GetCreno(
			@PathVariable long idmed,
			@PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

		try {
			Medecin medecin = medrepository.findById(idmed)
					.orElseThrow(() -> new BadRequestException("Médecin avec l'ID spécifié n'existe pas."));

			if (medecin.getUser() == null) {
				return ResponseEntity.ok(Collections.singletonMap("message", "Ce médecin n'est pas encore disponible sur Clinital"));
			}

			List<MedecinSchedule> schedules = medScheduleRepo.findByMedIdOrderByAvailability(idmed);
			List<FermetureExceptionnelle> fermetures = fermetureRepo.findByMedecinIdAndDateFinAfter(idmed, LocalDateTime.now());

			if (schedules.isEmpty()) {
				return ResponseEntity.ok(Collections.singletonMap("message", "Aucune disponibilité en ligne."));
			}

			if (startDate == null) startDate = LocalDate.now();
			LocalDate startOfWeek = getStartOfWeek(startDate);
			LocalDate endOfWeek = getEndOfWeek(startDate);
			LocalDate currentDay = LocalDate.now();
			LocalDateTime now = LocalDateTime.now();

			List<AgendaResponse> agendaResponseList = new ArrayList<>();

			for (LocalDate day = startOfWeek; !day.isAfter(endOfWeek); day = day.plusDays(1)) {
				LocalDate finalDay = day;
				List<MedecinSchedule> dailySchedules = schedules.stream()
						.filter(sch -> sch.getDay().getValue() == finalDay.getDayOfWeek().getValue())
						.toList();

				AgendaResponse agenda = new AgendaResponse();
				agenda.setDay(day.getDayOfWeek());
				agenda.setWorkingDate(day.atStartOfDay());

				if (dailySchedules.isEmpty() || day.isBefore(currentDay)) {
					agenda.setAvailableSlot(new ArrayList<>());
					agendaResponseList.add(agenda);
					continue;
				}

				List<String> allSlots = new ArrayList<>();
				List<HorairesResponse> horairesList = new ArrayList<>();
                ConsultationPeriodEnum period = null;
                List<ModeConsultation> modeconsultation = new ArrayList<>();

				for (MedecinSchedule sch : dailySchedules) {
					AgendaResponse partialAgenda = medecinService.CreateCreno(sch, new AgendaResponse(), idmed, 1, day.atStartOfDay());

					allSlots.addAll(partialAgenda.getAvailableSlot());
                    period = partialAgenda.getPeriod();
                    modeconsultation = partialAgenda.getModeconsultation();
					// Ajout des horaires pour info
					String startTime = sch.getAvailabilityStart().getHour() + ":" + String.format("%02d", sch.getAvailabilityStart().getMinute());
					String endTime = sch.getAvailabilityEnd().getHour() + ":" + String.format("%02d", sch.getAvailabilityEnd().getMinute());
					horairesList.add(new HorairesResponse(startTime, endTime));
				}

				LOGGER.info("Créneaux initiaux générés :");
				allSlots.forEach(s -> LOGGER.info("Créneau: {}", s));

				LocalDate finalDay1 = day;

				List<String> filteredSlots = allSlots.stream()
						.filter(slot -> {
							LocalTime slotTime = LocalTime.parse(slot);
							LocalDateTime slotDateTime = finalDay1.atTime(slotTime);
							LocalDateTime slotEndDateTime = slotDateTime.plusMinutes(15); // ou selon la durée

							// Un créneau est disponible si pour TOUTES les fermetures, il n'y a pas de chevauchement
							return fermetures.stream().noneMatch(fermeture -> {
								// Vérifier s'il y a chevauchement entre le créneau et la période de fermeture
								// Un chevauchement existe si le début du créneau est avant la fin de la fermeture
								// ET la fin du créneau est après le début de la fermeture
								boolean overlap = slotDateTime.isBefore(fermeture.getDateFin()) &&
										slotEndDateTime.isAfter(fermeture.getDateDebut());

								// Ajouter des logs pour le débogage
								System.out.println("Fermeture: " + fermeture.getDateDebut() + " → " + fermeture.getDateFin());
								System.out.println("Créneau: " + slotDateTime + " → " + slotEndDateTime);
								System.out.println("Chevauchement: " + overlap);

								return overlap;
							});
						})
						.collect(Collectors.toList());

				filteredSlots.forEach(s -> LOGGER.info("Créneau filtré: {}", s));
				agenda.setAvailableSlot(filteredSlots);
				agenda.setWorkingDate(day.atStartOfDay());
				agenda.setDay(day.getDayOfWeek());
				agenda.setWorkingHours(horairesList);
                agenda.setPeriod(period);
                //agenda.setModeconsultation(modeconsultation);

				agendaResponseList.add(agenda);
			}

			Set<AgendaResponse> set = new LinkedHashSet<>(agendaResponseList);
			agendaResponseList = new ArrayList<>(set);

			boolean hasAvailability = agendaResponseList.stream().anyMatch(a -> !a.getAvailableSlot().isEmpty());

			return hasAvailability
					? ResponseEntity.ok(agendaResponseList)
					: this.getNextAvailableAppointment(idmed);

		} catch (Exception e) {
			LOGGER.info("Erreur récupération agenda médecin : " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponse(false, e.getMessage()));
		}
	}


	public ResponseEntity<?> getNextAvailableAppointment(Long idmed) {
		List<MedecinSchedule> schedules = medScheduleRepo.findByMedId(idmed);

		if (schedules.isEmpty()) {
			return ResponseEntity.ok(Collections.singletonMap("message", "Aucune disponibilité en ligne."));
		}

		List<Rendezvous> futureAppointments = rdvRepository.findByMedecinIdAndStartAfterOrderByStartAsc(idmed, LocalDateTime.now());

		// Générer les futures dates disponibles
		LocalDate today = LocalDate.now();
		LocalDate nextWeekStart = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate searchLimit = nextWeekStart.plusYears(1); // On cherche sur 1 an à partir de la semaine prochaine
		LocalDate nextAvailableDate = null;

        while (nextWeekStart.isBefore(searchLimit)) {
			for (MedecinSchedule schedule : schedules) {
				if (schedule.getDay() == today.getDayOfWeek()) {
					LocalDateTime slotStart = today.atTime(schedule.getAvailabilityStart().toLocalTime());

					// Ajout de cette condition pour éviter de proposer un créneau déjà passé aujourd'hui
					if (slotStart.isBefore(LocalDateTime.now())) {
						continue;
					}

					boolean isTaken = futureAppointments.stream().anyMatch(rdv ->
							!rdv.getEnd().isBefore(slotStart) && !rdv.getStart().isAfter(slotStart.plusHours(1))
					);

					if (!isTaken) {
						nextAvailableDate = today;
						break;
					}
				}

			}
			if (nextAvailableDate != null) break;
			today = today.plusDays(1);
		}

		if (nextAvailableDate != null) {
			return ResponseEntity.ok(Collections.singletonMap("message", "Prochain RDV le " +
					nextAvailableDate.format(DateTimeFormatter.ofPattern("dd MM yyyy"))));
		} else {
			return ResponseEntity.ok(Collections.singletonMap("message", "Aucune disponibilité en ligne."));
		}
	}

    // Méthode utilitaire pour obtenir le début de la semaine
	private LocalDate getStartOfWeek(LocalDate date) {
		return date.with(DayOfWeek.MONDAY);
	}

	private LocalDate getEndOfWeek(LocalDate date) {
		return date.with(DayOfWeek.SUNDAY);
	}

	//-------FIN

	@GetMapping("/creneaux/{idmed}/{weeks}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public List<AgendaResponse> GetCreneau(@Validated @PathVariable long idmed, @PathVariable long weeks,
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

	}

	//****************

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
		List<MedecinDTO> medecinsDtos = medrepository.getAllMedecinsByCabinetId(idcab).stream()
				.map(follower -> mapper.map(follower, MedecinDTO.class))
				.toList();
		EquipeDTO equipe = new EquipeDTO();
		equipe.setSecretaires(secretaires);
		equipe.setAssistants(assistants);
		equipe.setMedecinDTOS(medecinsDtos);

//    activityServices.createActivity(new Date(), "Read", "Show Equipe for Medecin ",
//            globalVariables.getConnectedUser());
	    LOGGER.info("Show equipe medecin");
		return equipe;
	}

    @GetMapping("/medsBy-cabinet")
    public ResponseEntity<?> medecinForCabinet() throws Exception {
        Medecin medecin = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
        Long idcab = medecin.getFirstCabinetId();

        List<MedecinDTO> medecins = medrepository.getAllMedecinsByCabinetId(idcab).stream()
                .map(follower -> mapper.map(follower, MedecinDTO.class))
                .collect(Collectors.toList());

        LOGGER.info("Show equipe medecin");
        return ResponseEntity.ok().body(medecins);
    }

    //-----------------------------------------------NETWORK---------------------------------------------------
	// Add a New Doctor to the Network : %OK%
	@PostMapping("/addNewNetwork")
	public ResponseEntity<?> addNewNetwork(@Valid @RequestBody NetworkRequest network) throws Exception {
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
        LOGGER.info("Add Medecin by id {} for Medecin Connected, User ID: {}", network.getFollower_id(), globalVariables.getConnectedUser() != null ? globalVariables.getConnectedUser().getId() : "");
		return ResponseEntity.ok(mapper.map(medNet, MedecinNetwork.class));
	}


	@GetMapping("/checkIfInNetwork/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MEDECIN')")
	public ResponseEntity<?> checkIfInNetwork(@PathVariable(value="id") Long id) {
		try {
			Long userId = globalVariables.getConnectedUser().getId();
			Medecin connectedMedecin = medrepository.getMedecinByUserId(userId);

            LOGGER.info("Check if in network for followerId: {}", id);

			if (connectedMedecin == null) {
				LOGGER.warn("Aucun médecin trouvé pour l'utilisateur ID: " + userId);
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("Médecin introuvable pour l'utilisateur connecté.");
			}

			boolean isInNetwork = medrepository.findFollowerInNetwork(connectedMedecin.getId(), id) != null;
			return ResponseEntity.ok(Collections.singletonMap("inNetwork", isInNetwork));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de la vérification du médecin dans le réseau : " + e.getMessage());
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
	public ResponseEntity<?> deleteMedecinNetwork(@Valid @PathVariable Long follower_id) {
		try {
			if (follower_id == null || follower_id <= 0) {
				return ResponseEntity.badRequest().body("ID du follower invalide.");
			}

			Medecin currentMedecin = medrepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
			if (currentMedecin == null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Utilisateur non autorisé ou non médecin.");
			}

			Medecin follower = medrepository.getMedecinById(follower_id);
			if (follower == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le médecin follower spécifié n'existe pas.");
			}

			medecinNetworkService.deleteMedecinNetwork(currentMedecin.getId(), follower_id);
			return ResponseEntity.ok(new ApiResponse(true, "Le lien a été supprimé avec succès."));

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur interne lors de la suppression du lien : " + ex.getMessage());
		}
	}

	// Show all Network of a doc : %OK%
	@GetMapping("/getAllMedNetWork")
	public ResponseEntity<?> getAllMedecinNetwork() {
		try {
			Long userId = globalVariables.getConnectedUser().getId();

			Medecin med = medrepository.getMedecinByUserId(userId);
			if (med == null) {
				String message = "Médecin introuvable pour l'utilisateur connecté (ID: " + userId + ")";
				LOGGER.warn(message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
			}

			List<MedecinDTO> followers = medecinNetworkService.getAllMedecinNetwork(med.getId())
					.stream()
					.map(follower -> mapper.map(follower, MedecinDTO.class))
					.collect(Collectors.toList());

			activityServices.createActivity(
					new Date(), "Read",
					"Consult Medecin Network for Connected Medecin",
					globalVariables.getConnectedUser()
			);

			LOGGER.info("Consultation du réseau de médecins pour l'utilisateur ID: " + userId);
			return ResponseEntity.ok().body(followers);

		} catch (Exception e) {
			LOGGER.error("Erreur lors de la récupération du réseau de médecins : " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Une erreur est survenue lors de la récupération du réseau de médecins.");
		}
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


	@PutMapping("/photo-profil")
	public ResponseEntity<?> updatePhotoProfil(
			@RequestParam("image") MultipartFile image) {
		try {
			Long userId = globalVariables.getConnectedUser().getId();

			Medecin medecin = medrepository.getMedecinByUserId(userId);

			Medecin updated = medecinService.updatePhotoProfil(medecin.getId(), image);
			return ResponseEntity.ok(updated);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de l'envoi de la photo : " + e.getMessage());
		} catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medecin not found: " + e.getMessage());
        }
    }

	@PutMapping("/photo-couverture")
	public ResponseEntity<?> updatePhotoCouverture(
			@RequestParam("image") MultipartFile image) {
		try {
			Long userId = globalVariables.getConnectedUser().getId();
			Medecin medecin = medrepository.getMedecinByUserId(userId);
			if (medecin == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medecin not found");
			}
			Medecin updated = medecinService.updatePhotoCouverture(medecin.getId(), image);
			return ResponseEntity.ok(updated);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de l'envoi de la photo : " + e.getMessage());
		} catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medecin not found: " + e.getMessage());
        }
    }

	@PutMapping("/photo-couverture/byId/{id}")
	public ResponseEntity<?> updatePhotoCouvertureById(
			@RequestParam("image") MultipartFile image, @PathVariable Long id) {
		try {
			Medecin medecin = medrepository.getMedecinById(id);
			if (medecin == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medecin not found");
			}
			Medecin updated = medecinService.updatePhotoCouverture(medecin.getId(), image);
			return ResponseEntity.ok(updated);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de l'envoi de la photo : " + e.getMessage());
		}
	}

	@PutMapping("/photo-profil/byId/{id}")
	public ResponseEntity<?> updatePhotoProfilById(
			@RequestParam("image") MultipartFile image, @PathVariable Long id) {
		try {
			Medecin medecin = medrepository.getMedecinById(id);
			if (medecin == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medecin not found");
			}
			Medecin updated = medecinService.updatePhotoProfil(medecin.getId(), image);
			return ResponseEntity.ok(updated);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de l'envoi de la photo : " + e.getMessage());
		}
	}

	@DeleteMapping("/delete/photo-profil/byId/{id}")
	public ResponseEntity<?> deletePhotoProfil(@PathVariable Long id) {
		try {
			Medecin updatedMedecin = medecinService.deletePhotoProfil(id);
			return ResponseEntity.ok(updatedMedecin);
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Médecin introuvable.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de la suppression de la photo de profil.");
		}
	}

	@DeleteMapping("/delete/photo-couverture/byId/{id}")
	public ResponseEntity<?> deletePhotoCouverture(@PathVariable Long id) {
		try {
			Medecin updatedMedecin = medecinService.deletePhotoCouverture(id);
			return ResponseEntity.ok(updatedMedecin);
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Médecin introuvable.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de la suppression de la photo de couverture.");
		}
	}


	@DeleteMapping("/delete/photo-couverture")
	public ResponseEntity<?> deletePhotoCouverture() {
		try {
			Long userId = globalVariables.getConnectedUser().getId();
			Medecin medecin = medrepository.getMedecinByUserId(userId);
			if (medecin == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medecin not found");
			}
			medecinService.deletePhotoCouverture(medecin.getId());
			return ResponseEntity.ok("Photo de couverture supprimée avec succès.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de la suppression de la photo de couverture : " + e.getMessage());
		}
	}

	@DeleteMapping("/delete/photo-profil")
	public ResponseEntity<?> deletePhotoProfil() {
		try {
			Long userId = globalVariables.getConnectedUser().getId();
			Medecin medecin = medrepository.getMedecinByUserId(userId);
			if (medecin == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medecin not found");
			}
			medecinService.deletePhotoProfil(medecin.getId());
			return ResponseEntity.ok("Photo de profil supprimée avec succès.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors de la suppression de la photo de profil : " + e.getMessage());
		}
	}

}
