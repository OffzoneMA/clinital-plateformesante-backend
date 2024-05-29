package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.MedecinDTO;
import com.clinitalPlatform.dto.PatientDTO;
import com.clinitalPlatform.enums.CabinetStatuMedcinEnum;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.MedecinRequest;
import com.clinitalPlatform.payload.response.AgendaResponse;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.FichePatientResponse;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.util.ApiError;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Transactional
@Service
public class MedecinServiceImpl implements MedecinService {

	@Autowired
	private MedecinRepository medecinRepository;

	@Autowired
	private ClinitalModelMapper clinitalModelMapper;

	@Autowired
	ActivityServices activityServices;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	RendezvousService rendezvousService;

	@Autowired
	PatientService patientService;

	@Autowired
	EmailSenderService emailSenderService;

	@Autowired
	DemandeServiceImpl demandeServiceImpl;

	@Autowired
	DossierMedicalRepository dossierrepo;

	@Autowired
	UserRepository userRepo;

	@Autowired
    GlobalVariables globalVariables;

	@Autowired
	VilleRepository VilleRepository;
	@Autowired
	DipolmeMedecinRepository diplomerepo;
	@Autowired
	CabinetRepository cabinetrepo;
	@Autowired
	CabinetMedecinRepository cabmedrepo;
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	
	@Override
	public MedecinDTO create(MedecinRequest request) throws Exception {
	try {
	
		//UserDetailsImpl ConnectedUser=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Cabinet cabinet=cabinetrepo.findById(request.getCabinet()).orElseThrow(()->new Exception("No Matching Cabinet"));
		Medecin med=medecinRepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		Boolean IsAllowedtoAdd=CabinetMedecinRepository.isAllowed(med.getId(), cabinet.getId_cabinet());

		if(IsAllowedtoAdd){

			Medecin medecin = medecinRepository.findById(request.getId()).orElseThrow(()->new Exception("No Matching Medecin"));
			CabinetMedecinsSpace medcab=new CabinetMedecinsSpace();
			medcab.setMedecin(medecin);
			medcab.setCabinet(cabinet);
			medcab.setStatus(CabinetStatuMedcinEnum.USER);
			cabmedrepo.save(medcab);
					cabinet.getMedecin().add(medcab);
					cabinetrepo.save(cabinet);

						activityServices.createActivity(new Date(),"Add","Add New Medecin By Medecin Admin to cabinet ID :"+cabinet.getId_cabinet(),globalVariables.getConnectedUser());
						LOGGER.info("Add New Medecin By Medecin Admin to cabinet ID :"+cabinet.getId_cabinet()+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

					return clinitalModelMapper.map(medecin, MedecinDTO.class);
	} else 
	LOGGER.info("You are not Allowed to add new Medecin, User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
	throw new Exception("Your are not Allowed");
	
} catch (Exception e) {
	throw new Exception(e.getMessage());
}
	
			


	
	}

	@Override
	public MedecinDTO update(MedecinRequest request, Long id) throws Exception {
		try {
			UserDetailsImpl ConnectedUser=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User user= userRepo.getById(ConnectedUser.getId());
			Medecin medecin=medecinRepository.getMedecinByUserId(ConnectedUser.getId());
			Ville ville=VilleRepository.findById(request.getVille()).orElseThrow(()->new Exception("No Matching Ville"));
			DiplomeMedecin diplome=diplomerepo.findById(request.getDiplome_med()).orElseThrow(()->new Exception("No Matching Diplome"));
		
				medecin.setNom_med(request.getNom_med());
				medecin.setPrenom_med(request.getPrenom_med());
				medecin.setMatricule_med(request.getMatricule_med());
				medecin.setInpe(request.getInpe());
				medecin.setPhoto_med(null);
				medecin.setPhoto_couverture_med(null);
				medecin.setDescription_med(request.getDescription_med());
				medecin.setContact_urgence_med(request.getContact_urgence_med());
				medecin.setCivilite_med(request.getCivilite_med());	
				medecin.setUser(user);
				medecin.setVille(ville);
				medecin.getDiplome_med().add(diplome);
				medecin.setIsActive(false);
				medecinRepository.save(medecin);
				activityServices.createActivity(new Date(),"Update","Update Medecin  ID : "+id,globalVariables.getConnectedUser());
				LOGGER.info("Update Medecin ID : "+id+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
			return clinitalModelMapper.map(medecin, MedecinDTO.class);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
	}

	@Override
	public List<MedecinDTO> findAll() {
		return medecinRepository
				.findAll()
				.stream()
				.map(med -> clinitalModelMapper.map(med, MedecinDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	public Medecin findById(Long id) throws Exception {
		Medecin med = medecinRepository.findbyid(id).orElseThrow(() -> new Exception("Medecin not found"));
		if(globalVariables.getConnectedUser()!=null){
			activityServices.createActivity(new Date(),"Read","Consult Medecin By ID : "+id,globalVariables.getConnectedUser());
			LOGGER.info("Consult Medecin By ID : "+id+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		}else{
			LOGGER.info("Consult Medecin By ID : "+id);
		}
		return clinitalModelMapper.map(med, Medecin.class);
	}

	@Override
	public void deleteById(Long id) throws Exception {
		Optional<Medecin> med = medecinRepository.findById(id);
		if (med.isPresent()) {
			// save activity Delete medecin
			User user = medecinRepository.findById(id).get().getUser();
			activityServices.createActivity(new Date(), "Delete", "Suppression Compte Medcin", user);
			medecinRepository.deleteById(id);
		} else
			throw new Exception("Medecin not found");

	}

	@Override
	public List<PatientDTO> getMedecinPatients(Long id) throws Exception {
		Medecin med = medecinRepository.getMedecinByUserId(id);
		return patientRepository.getMedecinPatients(med.getId()).stream()
				.map(patient -> clinitalModelMapper.map(patient, PatientDTO.class)).collect(Collectors.toList());
	}

	@Override
	public PatientDTO getPatient(Long iduser, long idpat) throws Exception {
		Patient pat = patientRepository.findById(idpat).orElseThrow(() -> new Exception("Patient not found"));
		Medecin med = medecinRepository.getMedecinByUserId(iduser);
		Patient patient = patientRepository.getPatient(pat.getId(), med.getId());
		return clinitalModelMapper.map(patient, PatientDTO.class);
	}

	@Override
	public Medecin getMedecinByUserId(long id) throws Exception {

		Medecin med = medecinRepository.getMedecinByUserId(id);

		return clinitalModelMapper.map(med, Medecin.class);
	}

	// Creating a creno.
//	public AgendaResponse CreateCreno(MedecinSchedule Medsch, AgendaResponse agenda, long idmed, long week,
//			LocalDateTime Date) {
//
//		long minutes = ChronoUnit.MINUTES.between(Medsch.getAvailabilityStart(),
//				Medsch.getAvailabilityEnd());
//
//		long totalSlots = minutes / Medsch.getPeriod().getValue();
//
//		LocalDateTime timer = Medsch.getAvailabilityStart();
//
//		agenda.setDay(Medsch.getDay());
//		agenda.setWorkingDate(Date);
//		agenda.setWeek(week);
//		agenda.setPeriod(Medsch.getPeriod());
//		agenda.setIsnewpatient(Medsch.getIsnewpatient());
//		agenda.setMotifconsultation(Medsch.getMotifConsultation());
//		agenda.setModeconsultation(Medsch.getModeconsultation());
//		List<Rendezvous> rendezvous = rendezvousService.findRendezvousByMedAndDate(idmed, Date);
//		List<RendezvousResponse> rdvrespo = rendezvous.stream()
//				.map(rdv -> clinitalModelMapper.map(rdv, RendezvousResponse.class)).collect(Collectors.toList());
//		// agenda.getAvailableSlot()
//		// .add((timer.getHour() < 10 ? "0" : "") + timer.getHour() + ":"
//		// + (timer.getMinute() < 10 ? "0" : "") + timer.getMinute());
//
//		for (int j = 0; j < totalSlots; j++) {
//			if (!rendezvous.isEmpty()) {
//				for (RendezvousResponse rdv : rdvrespo) {
//					int index = rdvrespo.indexOf(rdv);
//					// if(rdv.getStart().getDayOfMonth()!=rdv.getEnd().getDayOfMonth()){
//					// long days = ChronoUnit.DAYS.between(rdv.getStart(),rdv.getEnd());
//					// for(int i=0;i>days;i++){
//
//					// }
//
//					// }
//					if (rdv.getStatut().equals(RdvStatutEnum.CONJE)) {
//
//						continue;
//
//					} else if (rdv.getStart().getHour() == timer.getHour()
//							&& rdv.getStart().getMinute() == timer.getMinute()
//							&& rdv.getStart().toLocalDate().isEqual(Date.toLocalDate())) {
//						// agenda.getAvailableSlot()
//						// .add("Unavailible");
//
//						if (rdv.getStart().isBefore(rdv.getEnd())) {
//							agenda.getAvailableSlot()
//									.add("Rsrvd" + rdv.getStart());
//							rdvrespo.set(index, rdv);
//							rdv.setStart(rdv.getStart().plusMinutes(Medsch.getPeriod().getValue()));
//							break;
//							// if(!rdv.getStart().isEqual(rdv.getEnd())){
//
//							// }
//
//						}else {
//							agenda.getAvailableSlot()
//									.add((timer.getHour() < 10 ? "0" : "") + timer.getHour() + ":"
//											+ (timer.getMinute() < 10 ? "0" : "") + timer.getMinute());}
//
//					} else {
//						agenda.getAvailableSlot()
//								.add((timer.getHour() < 10 ? "0" : "") + timer.getHour() + ":"
//										+ (timer.getMinute() < 10 ? "0" : "") + timer.getMinute());
//					}
//
//				}
//
//			} else {
//				agenda.getAvailableSlot()
//						.add((timer.getHour() < 10 ? "0" : "") + timer.getHour() + ":"
//								+ (timer.getMinute() < 10 ? "0" : "") + timer.getMinute());
//
//			}
//
//			timer = timer.plusMinutes(Medsch.getPeriod().getValue());
//
//		}
//
//		return agenda;
//
//	}

	// It's returning the number of days in a month.
	public int getDaysInMonth(LocalDateTime localDateTime) {

		int daysInMonth = 0;
		int year = localDateTime.getYear();
		int month = localDateTime.getMonth().getValue();

		switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				daysInMonth = 31;
				break;
			case 4:
			case 6:
			case 9:
			case 11:
				daysInMonth = 30;
				break;
			case 2:
				if (((year % 4 == 0) && !(year % 100 == 0) || (year % 400 == 0))) {
					daysInMonth = 29;
				} else {
					daysInMonth = 28;
				}
				break;
			default:
				System.out.println("Invalid month");
				break;
		}

		return daysInMonth;
	}

	// Setting the visibility of the medecin.
	public Medecin setVisibiltyMedecin(long id) {

		Optional<Medecin> ismed = medecinRepository.findById(id);
		if (ismed.isPresent()) {

			Medecin med = medecinRepository.getMedecinById(id);
			medecinRepository.setVisibelityMedecin(!med.getIsActive(), med.getId());
			return med;
		}
		return null;
	}
	// Send request to Ptient to grant docotr persmession to access his folder.
//	public String  Askpermission(Patient patient) throws Exception{
//
//		try {
//
//						String code = demandeServiceImpl.SecretCode();
//						DossierMedical dossier = patient.getDossierMedical();
//						dossier.setAccesscode(code);
//						dossierrepo.save(dossier);
//						String result = emailSenderService.sendMailCodeAccess(patient.getPatientEmail(), code);
//						return result;
//		} catch (Exception e) {
//			throw new Exception(e.getMessage());
//		}
//
	//}
	// Access to Folder of a patient
//	public ResponseEntity<?> GetaccessToFolder(Long iddoss,String code) throws Exception, ApiError{
//
//		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
//        //         .getPrincipal();
//		// Get Medecin User connected
//		Medecin medecin=medecinRepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
//		// get Patient relited to the given medical folder ID
//		Patient patient = patientRepository.findPatientByDossMedicale(iddoss).orElseThrow(()->new Exception(" NO MATCHING FOUND ! "+iddoss));
//
//		// check if this medecin has this folder before
//		Boolean isDosMedical = medecin.getMeddossiers().stream().filter(dossier->dossier.getId_dossier()==iddoss).findFirst().isPresent();
//
//		if(isDosMedical){
//			//is so send it back
//			FichePatientResponse FichePatResponse=patientService.GenrateFichepatient(patient.getId(),medecin);
//
//            return ResponseEntity.ok(FichePatResponse) ;
//        }else{
//			// check if this doc has any recent rdv with this pation
//			List<Rendezvous> AllRendezvous=rendezvousService.getRdvByIdMedecinandIdPatient(medecin.getId(),patient.getId()).stream().map(doc -> clinitalModelMapper.map(doc, Rendezvous.class)).collect(Collectors.toList());
//			//check if this Med has eny rdv with the patient
//				if(!AllRendezvous.isEmpty()){
//					System.out.println("code access : "+code+" "+patient.getDossierMedical().getAccesscode());
//					if(code!=""){
//						if(patient.getDossierMedical().getAccesscode().toLowerCase().equals(code.toLowerCase())){
//							patientService.ShareMedecialFolder(patient.getDossierMedical().getId_dossier(), medecin.getId());
//							FichePatientResponse FichePatResponse=patientService.GenrateFichepatient(patient.getId(),medecin);
//
//            				return ResponseEntity.ok(FichePatResponse) ;
//						}else return ResponseEntity.ok(new ApiResponse(false, "Your Code access is not Valid plz verify"));
//
//						}else{
//							//if so send and SMS or Email to the patient with a code to ask him grant to access his medical folder.
//						return  ResponseEntity.ok(this.Askpermission(patient));
//						}
//
//					}else return ResponseEntity.ok(new ApiResponse(false, "this Folder is not accisseble !!You Dont have any rendez vous yet with this patient"));
//
//		}
//
//	}
public Boolean isAccessibleFolder(Long iddoss) throws Exception{
		try {
		// UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        //         .getPrincipal();
		// Get Medecin User connected
		Medecin medecin=medecinRepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
		// check if this medecin has this folder before
		DossierMedical dossierMedical=dossierrepo.findById(iddoss).orElseThrow(()->new Exception("No Matching Found"));
		Boolean isDosMedical = medecin.getMeddossiers().stream().filter(dossier->dossier.getId_dossier()==dossierMedical.getId_dossier()).findFirst().isPresent();
			activityServices.createActivity(new Date(),"Read","Checking if has right to accessto this Folder ID : "+dossierMedical.getId_dossier(),globalVariables.getConnectedUser());
			LOGGER.info("Checking if has right to access to this Folder ID : "+dossierMedical.getId_dossier()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return isDosMedical;
		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}
	}
// Uplaod profile picture of a doctor :
	
public void UploadProfilePicture(){
	
}
// my visibility 
public ResponseEntity<Medecin> Myvisibity(Medecin med)throws Exception{
	try{
		med.setIsActive(!med.getIsActive());
		activityServices.createActivity(new Date(),"Update","Chaning Visibilty of Account to :"+!med.getIsActive(),globalVariables.getConnectedUser());
			LOGGER.info("Chaning Visibilty of Account to :"+!med.getIsActive()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		return ResponseEntity.ok(med);
	}
	catch(Exception e){
       throw new Exception(e.getMessage());
	}
}
	


	

}
