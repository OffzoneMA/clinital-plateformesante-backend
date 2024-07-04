package com.clinitalPlatform.services;

import com.clinitalPlatform.dao.IDao;
import com.clinitalPlatform.dto.RendezvousDTO;
import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.security.config.VideoCall.UrlVideoCallGenerator;
import com.clinitalPlatform.util.ApiError;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Service
@Transactional(propagation = Propagation.REQUIRED)
//@Primary
public class RendezvousService implements IDao<Rendezvous>  {

	@Autowired
	private RdvRepository rdvrepo;

	@Autowired
	private DocumentRepository docrepo;
	@Autowired
	private PatientRepository patientRepo;
	@Autowired
	private MedecinRepository medRepo;
	@Autowired
	private ModeConsultRespository moderespo;
	@PersistenceContext
	private EntityManager entityManger;
	@Autowired
	private SpecialiteRepository specialiteRepository;
	@Autowired
	private MotifCondultationRepository mRepository;
	@Autowired
	private CabinetRepository cabrepo;
	@Autowired
	private ClinitalModelMapper mapper;

	@Autowired
	private ActivityServices ActivityServices;
	@Autowired
	private GlobalVariables globalVariables;
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	@Autowired
	private UrlVideoCallGenerator urlVideoCallGenerator;
	@Override
	public Rendezvous create(Rendezvous o) {
		return rdvrepo.save(o);

	}

	@Override
	public void update(Rendezvous o) {
		rdvrepo.save(o);
	}





	@Override
	public void delete(Rendezvous o) {
		rdvrepo.delete(o);

	}

	public void deleteRendezvous(Long idrdv) throws Exception {


		Optional<Rendezvous> rdv = rdvrepo.findById(idrdv);



		if (rdv.isPresent()){
			List<Document> docs = docrepo.getDocByIdRendezvous(idrdv);

			docs.forEach(doc -> doc.setRendezvous(null));

			rdvrepo.DeletRdvByIdProfile(idrdv);

		}
		else
		{

			throw new Exception("Fail to delete");

		}


	}
//	public Rendezvous updateRendezvous(RendezvousDTO rendezvousDTO) throws Exception {
//
////		Rendezvous existingRendezvous = rdvrepo.findById(rendezvousDTO.getId())
////				.orElseThrow(() -> new Exception("Rendezvous not found with id: " + rendezvousDTO.getId()));
////		Medecin med=medRepo.getMedecinById(rendezvousDTO.getMedecinid());
////		Cabinet cab=cabrepo.getById(rendezvousDTO.getCabinet());
////		ModeConsultation modconsu=moderespo.getById(rendezvousDTO.getModeconsultation());
////		MotifConsultation motifcons=mRepository.getById(rendezvousDTO.getMotif());
////		Patient p=patientRepo.getById(rendezvousDTO.getPatientid());
////		// Update the existing rendezvous with new values
////		existingRendezvous.setId(rendezvousDTO.getId());
////		existingRendezvous.setDay(rendezvousDTO.getDay());
////		existingRendezvous.setStart(rendezvousDTO.getStart());
////		existingRendezvous.setEnd(rendezvousDTO.getEnd());
////		existingRendezvous.setCanceledAt(rendezvousDTO.getCanceledat());
////		existingRendezvous.setStatut(rendezvousDTO.getStatut());
////		existingRendezvous.setMedecin(med);
////		existingRendezvous.setCabinet(cab);
////		existingRendezvous.setModeConsultation(modconsu);
////		existingRendezvous.setMotifConsultation(motifcons);
////		existingRendezvous.setPatient(p);
////		existingRendezvous.setCommantaire(rendezvousDTO.getCommantaire());
////		existingRendezvous.setISnewPatient(rendezvousDTO.getIsnewpatient());
////		existingRendezvous.setLinkVideoCall(rendezvousDTO.getLinkVideoCall());
////		// Update other attributes as needed
////
////		return rdvrepo.UpdateRdvByIdRdv();
//
//	}

	public RendezvousDTO convertToDTO(Rendezvous rendezvous, RendezvousDTO dto) {
		dto.setId(rendezvous.getId());
		dto.setDay(rendezvous.getDay());
		dto.setStart(rendezvous.getStart());
		dto.setEnd(rendezvous.getEnd());
		dto.setCanceledat(rendezvous.getCanceledAt());
		dto.setStatut(rendezvous.getStatut());
		dto.setModeconsultation(rendezvous.getModeConsultation().getId_mode());
		dto.setMedecinid(rendezvous.getMedecin().getId());
		dto.setPatientid(rendezvous.getPatient().getId());
		dto.setIsnewpatient(true);
		dto.setCommantaire(rendezvous.getCommantaire());
		dto.setMotif(rendezvous.getMotifConsultation().getId_motif());
		dto.setLinkVideoCall(rendezvous.getLinkVideoCall());
		dto.setCabinet(rendezvous.getCabinet().getId_cabinet());

		return dto;
	}

	public Rendezvous updateRendezvousById(Long id, RendezvousDTO updateDTO) throws Exception {
		Rendezvous existingRendezvous = rdvrepo.findById(id)
				.orElseThrow(() -> new Exception("Rendezvous not found with id: " + id));
//
//		// Update properties from DTO
//		BeanUtils.copyProperties(updateDTO, existingRendezvous, "id");

		// Save and return updated rendezvous
		return existingRendezvous;
	}

	public void UpdateRdvByIdPatient(com.clinitalPlatform.payload.request.@Valid RendezvousRequest req, long id) throws Exception{
		DayOfWeek day = DayOfWeek.valueOf(req.getDay());
		rdvrepo.UpdateRdvByIdProfile(day,req.getStart(),req.getStatut().toString(),req.getMedecinid(),req.getPatientid(),req.getMotif().toString(),req.getEnd(), id,req.getModeconsultation());

	}

//	public void UpdateRdvByIdMedecin(RendezvousRequest req,long id,long idMed) throws Exception{
//		DayOfWeek day = DayOfWeek.valueOf(req.getDay());
//
//		rdvrepo.UpdateRdvByIdProfile(day,req.getStart(),req.getStatut().toString(),idMed,req.getPatientid(),req.getMotif().toString(),req.getEnd(), id,req.getModeconsultation());
//
//	}

	public ResponseEntity<?> UpdateRdvdate(com.clinitalPlatform.payload.request.@Valid RendezvousRequest req, long id) throws Exception{
		//DayOfWeek day = DayOfWeek.valueOf(req.getDay());

		DayOfWeek day = req.getStart().getDayOfWeek();
		Optional<Rendezvous> isrdv=rdvrepo.IsRdvinDateStart(req.getStart(),day.getValue()-1,req.getMedecinid());
		if(isrdv.isPresent()){

			ActivityServices.createActivity(new Date(), "Update", "You Cant Change this Rdv : "+id,
					globalVariables.getConnectedUser());
			LOGGER.info("You Cant Change this Rdv , UserID : " + globalVariables.getConnectedUser().getId());

			return ResponseEntity.accepted().body(new ApiError(HttpStatus.FORBIDDEN, "You cant update this date", null));

		} else {
			ActivityServices.createActivity(new Date(), "Update", "Update Rdv for Patient",
					globalVariables.getConnectedUser());
			LOGGER.info("Update Rdv for Patient, UserID : " + globalVariables.getConnectedUser().getId());
			rdvrepo.UpdateRdvdatestart(day, req.getStart(), req.getEnd(), id);
			return ResponseEntity.ok(rdvrepo.getById(id)) ;
		}

	}

	@Override
	public List<Rendezvous> findAll() {
		return rdvrepo.findAll();
	}

	@Override
	public Optional<Rendezvous> findById(long id) {
		return rdvrepo.findById(id);
	}

	public List<Rendezvous> getRendezvousById(long id){
		return rdvrepo.getRendezvousById(id);
	}

	public List<Rendezvous> findRendezvousByMedAndDate(Long medecinId, LocalDateTime date) {
//		ZoneId systemTimeZone = ZoneId.systemDefault();

//		ZonedDateTime zonedDateTimeFrom = fromDate.atStartOfDay(systemTimeZone);
//		ZonedDateTime zonedDateTimeTo = toDate.atStartOfDay(systemTimeZone);
//
//		Date startDate = Date.from(zonedDateTimeFrom.toInstant());
//		Date endDate = Date.from(zonedDateTimeTo.toInstant());

		return rdvrepo.findByDateAndMedecin(date.toLocalDate(), medecinId).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class)).collect(Collectors.toList());

	}
	public List<Rendezvous> findRendezvousByPatientAndDate(Long patId, LocalDateTime fromDate) {
		return rdvrepo.findByDateAndPatient(fromDate, patId).stream()
				.map(rdv -> mapper.map(rdv, Rendezvous.class)).collect(Collectors.toList());

	}



	//RDV BY DATE (DAR,WEEK,MONTH,YEAR) For A DOCTOR :
	//Get Rdv By Day :
	public List<Rendezvous> getRdvMedByDay(@Valid long day,long id){
		return rdvrepo.getRendezvousPatientByDay(day,id);
	}
	//Get Rdv By Day :
	public List<Rendezvous> getRdvMedByWeek(long week,long id){
		return rdvrepo.getRendezvousMedByWeek(week,id);
	}
	//Get Rdv By Day :
	public List<Rendezvous> getRdvMedByMonth(long month,long id){
		return rdvrepo.getRendezvousMedByMonth(month,id);
	}

	//Get Rdv By Day :
	public List<Rendezvous> getRdvMedByYear(long year,long id){
		return rdvrepo.getRendezvousMedByYear(year,id);
	}

//RDV BY DATE (DAR,WEEK,MONTH,YEAR) For A Patient :

	//Get Rdv By Day of Week :
	public List<Rendezvous> getRdvPatientByDayWeek(long day,long id){
		return rdvrepo.getRendezvousPatientByDayofweek(day,id);
	}
	//Get Rdv By Day :
	public List<Rendezvous> getRdvPatientByDay(long day,long id){
		return rdvrepo.getRendezvousPatientByDay(day,id);
	}
	//Get Rdv By Day :
	public List<Rendezvous> getRdvPatientByWeek(long week,long id){
		return rdvrepo.getRendezvousPatientByWeek(week,id);
	}
	//Get Rdv By Day :

	public List<Rendezvous> getRdvPatientByMonth(long month,long id){
		return rdvrepo.getRendezvousPatientByMonth(month,id);
	}
	//Get Rdv By Day :
	public List<Rendezvous> getRdvPatientByYear(long year,long id){
		return rdvrepo.getRendezvousPatientByYear(year,id);
	}

	//RDV by Id User and Patient ID :
	public List<Rendezvous> findRdvByIdUserandPatient(long iduser,long idpatient){
		return rdvrepo.findRdvByIduserandPatient(iduser, idpatient)
				.stream().map(pat->mapper.map(pat, Rendezvous.class))
				.collect(Collectors.toList());

	}

	//RDV by Id and Patient ID :
	public Rendezvous findRdvByIdUserandId(long iduser,long id){
		return rdvrepo.findRdvByIdUserandId(iduser, id);

	}

	public List<Rendezvous> getRdvByIdMedecinandIdPatient(long idmed,long patid){
		return rdvrepo.getRdvByIdMedecinandIdPatientandDate(idmed,patid);
	}

	public boolean isExistingRDVForMedecinAndDay(Long idpat, Long medecinId,LocalDate day) throws Exception {
		// Retrieve Medecin entity
		Optional<Medecin> optionalMedecin = medRepo.findById(medecinId);
		if (optionalMedecin.isEmpty()) {
			LOGGER.info("there is no medecin founded");
			return false;
		}
		Medecin medecin = optionalMedecin.get();

		// Retrieve RDVs for the same day and medecin's specialty
		List<Rendezvous> existingRDVs = rdvrepo.findRdvByPatientandSpecInDate(idpat,medecin.getSpecialite().getId_spec(),day);

		// Check if any existing RDVs were found
		return !existingRDVs.isEmpty();
	}

//	public ResponseEntity<?> AddnewRdvgestionregle(User user,RendezvousDTO c,Medecin medecin,Patient patient) throws Exception{
//		try {
//			// DayOfWeek day = DayOfWeek.valueOf(c.getDay());
//			MotifConsultation motif = mRepository.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Motif"));
//			ModeConsultation mode =moderespo.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Mode consultation"));
//			Cabinet cabinet=cabrepo.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a cabinet"));
//			Boolean isReserved = false,ModeMedecin=false;
//			isReserved=	this.isHasRdvToday( medecin.getSpecialite().getId_spec(), c.getStart().toLocalDate());
//			ModeMedecin=isReserved?false:true;
//
//			if (!isReserved) {
//
//				// DayOfWeek day = DayOfWeek.valueOf(c.getDay());
//				Rendezvous rendezvous = new Rendezvous();
//				rendezvous.setId(c.getId());
//				rendezvous.setMedecin(medecin);
//				rendezvous.setMotifConsultation(motif);
//				rendezvous.setPatient(patient);
//				rendezvous.setDay(c.getDay());
//				rendezvous.setStart(c.getStart());
//				rendezvous.setEnd(c.getEnd());
//				rendezvous.setStatut(c.getStatut());
//				rendezvous.setCanceledAt(c.getCanceledat());
//				rendezvous.setModeConsultation(mode);
//				rendezvous.setISnewPatient(c.getIsnewpatient());
//				if (user != null && rendezvous != null) {
//					ERole userRole = user.getRole();
//					if (userRole == ERole.ROLE_MEDECIN && c != null && c.getCommantaire() != null) {
//						rendezvous.setCommantaire(c.getCommantaire());
//					}
//				}
//				rendezvous.setLinkVideoCall(urlVideoCallGenerator.joinConference());
//				rendezvous.setCabinet(cabinet);
//				LOGGER.info("id="+ rendezvous.getId());
//				//rdvrepo.save(rendezvous);
//				entityManger.persist(rendezvous);
//				ActivityServices.createActivity(new Date(),"Add","Add New Rdv ",user);
//				LOGGER.info("Add new Rdv, UserID : "+user.getId());
//				return ResponseEntity.ok(mapper.map(rendezvous, Rendezvous.class));
//
//			} else
//				return ResponseEntity.ok(new ApiResponse(false, "You have already an other RDV with "));
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			throw new Exception(e.getMessage());
//		}
//	}



	//Add RDV :
	public ResponseEntity<?> AddnewRdv(User user,RendezvousDTO c,Medecin medecin,Patient patient) throws Exception{

		try {
			// DayOfWeek day = DayOfWeek.valueOf(c.getDay());
			MotifConsultation motif = mRepository.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Motif"));
			ModeConsultation mode =moderespo.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Mode consultation"));
			Cabinet cabinet=cabrepo.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a cabinet"));
			Boolean isReserved = false,ModeMedecin=false;
			isReserved=	this.isHasRdvToday( medecin.getSpecialite().getId_spec(), c.getStart().toLocalDate());
			RendezvousDTO rdvReserved=this.getRdvToday( medecin.getSpecialite().getId_spec(), c.getStart().toLocalDate());

			ModeMedecin=isReserved?false:true;

			if (!isReserved||ModeMedecin) {

				// DayOfWeek day = DayOfWeek.valueOf(c.getDay());
				Rendezvous rendezvous = new Rendezvous();
//		rendezvous.setId(c.getId());
				rendezvous.setMedecin(medecin);
				rendezvous.setMotifConsultation(motif);
				rendezvous.setPatient(patient);
				rendezvous.setDay(c.getDay());
				rendezvous.setStart(c.getStart());
				rendezvous.setEnd(c.getEnd());
				rendezvous.setStatut(c.getStatut());
				rendezvous.setCanceledAt(c.getCanceledat());
				rendezvous.setModeConsultation(mode);
				rendezvous.setISnewPatient(c.getIsnewpatient());
				if (user != null && rendezvous != null) {
					ERole userRole = user.getRole();
					if (userRole == ERole.ROLE_MEDECIN && c != null && c.getCommantaire() != null) {
						rendezvous.setCommantaire(c.getCommantaire());
					}
				}
				rendezvous.setLinkVideoCall(urlVideoCallGenerator.joinConference());
				rendezvous.setCabinet(cabinet);
				LOGGER.info("id="+ rendezvous.getId());
				//rdvrepo.save(rendezvous);
				entityManger.persist(rendezvous);
				ActivityServices.createActivity(new Date(),"Add","Add New Rdv ",user);
				LOGGER.info("Add new Rdv, UserID : "+user.getId());
				return ResponseEntity.ok(mapper.map(rendezvous, Rendezvous.class));

			} else
				return ResponseEntity.ok(new ApiResponse(false, "You have already an other RDV ",rdvReserved));

		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}
	}
	public Rendezvous updateerdv(long rdvId,RendezvousDTO rdvDTO, Medecin medecin, Patient patient) throws Exception{
		Rendezvous rdv = rdvrepo.findById(rdvId).orElseThrow(() -> new Exception("RDV not found"));

		MotifConsultation motif = mRepository.findById(rdvDTO.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Motif"));
		ModeConsultation mode =moderespo.findById(rdvDTO.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Mode consultation"));
		Cabinet cabinet=cabrepo.findById(rdvDTO.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a cabinet"));
		rdv.setId(rdvDTO.getId());
		rdv.setCanceledAt(rdvDTO.getCanceledat());
		rdv.setDay(rdvDTO.getDay());
		rdv.setStart(rdvDTO.getStart());
		rdv.setEnd(rdvDTO.getEnd());
		rdv.setMedecin(medecin);
		rdv.setMotif(motif);
		rdv.setPatient(patient);
		rdv.setStatut(rdvDTO.getStatut());
		rdv.setModeConsultation(mode);
		rdv.setISnewPatient(rdvDTO.getIsnewpatient());
		rdv.setCommantaire(rdvDTO.getCommantaire());
		rdv.setCabinet(cabinet);
		return rdvrepo.save(rdv);

	}


	public ResponseEntity<?> Updaterdv(User user,RendezvousDTO c,Medecin medecin,Patient patient,@Param("id") long id) throws Exception{

		try {
			// DayOfWeek day = DayOfWeek.valueOf(c.getDay());
			MotifConsultation motif = mRepository.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Motif"));
			ModeConsultation mode =moderespo.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a Mode consultation"));
			Cabinet cabinet=cabrepo.findById(c.getCabinet()).orElseThrow(()->new Exception("No such Id exist for a cabinet"));
			// DayOfWeek day = DayOfWeek.valueOf(c.getDay());
			Rendezvous rendezvous = (Rendezvous) rdvrepo.getRendezvousById(id);
			rendezvous.setMedecin(medecin);
			rendezvous.setMotifConsultation(motif);
			rendezvous.setPatient(patient);
			rendezvous.setDay(c.getDay());
			rendezvous.setStart(c.getStart());
			rendezvous.setEnd(c.getEnd());
			rendezvous.setStatut(c.getStatut());
			rendezvous.setCanceledAt(c.getCanceledat());
			rendezvous.setModeConsultation(mode);
			rendezvous.setISnewPatient(c.getIsnewpatient());
			if (user != null && rendezvous != null) {
				ERole userRole = user.getRole();
				if (userRole == ERole.ROLE_MEDECIN && c != null && c.getCommantaire() != null) {
					rendezvous.setCommantaire(c.getCommantaire());
				}
			}
			rendezvous.setLinkVideoCall(urlVideoCallGenerator.joinConference());
			rendezvous.setCabinet(cabinet);
			//rdvrepo.save(rendezvous);
			entityManger.persist(rendezvous);
			ActivityServices.createActivity(new Date(),"Update","Update  Rdv ",user);
			LOGGER.info("Update new Rdv, UserID : "+user.getId());
			return ResponseEntity.ok(mapper.map(rendezvous, Rendezvous.class));



		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}
	}
//	private boolean hasExistingAppointment(Long patientId, Long specialtyId, LocalDate appointmentDate) {
//		// Query the database to check if there is an existing appointment for the patient
//		// with the same specialty on the same day
//		List<Rendezvous> existingAppointments = rdvrepo.findByPatientIdAndMedecinSpecialiteIdAndDay(patientId, specialtyId, appointmentDate.getDayOfWeek());
//
//		// Iterate through the existing appointments and check if any of them falls on the same day
//		for (Rendezvous appointment : existingAppointments) {
//			LocalDateTime startDateTime = appointment.getStart();
//			LocalDateTime endDateTime = appointment.getEnd();
//			LocalDate appointmentStartDate = startDateTime.toLocalDate();
//			LocalDate appointmentEndDate = endDateTime.toLocalDate();
//
//			// Check if the appointment date falls within the same day
//			if (appointmentStartDate.isEqual(appointmentDate) || appointmentEndDate.isEqual(appointmentDate)) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	public ResponseEntity<?> AddnewRdv(User user, RendezvousDTO c, Medecin medecin, Patient patient) throws Exception {
//		try {
//			MotifConsultation motif = mRepository.findById(c.getCabinet()).orElseThrow(() -> new Exception("No such Id exist for a Motif"));
//			ModeConsultation mode = moderespo.findById(c.getCabinet()).orElseThrow(() -> new Exception("No such Id exist for a Mode consultation"));
//			Cabinet cabinet = cabrepo.findById(c.getCabinet()).orElseThrow(() -> new Exception("No such Id exist for a cabinet"));
//
//			// Check if the patient already has an appointment with the same specialty on the same day
//			boolean hasExistingAppointment = hasExistingAppointment(patient.getId(), medecin.getSpecialite().getId_spec(), c.getStart().toLocalDate());
//
//			if (!hasExistingAppointment) {
//				Rendezvous rendezvous = new Rendezvous();
//				rendezvous.setMedecin(medecin);
//				rendezvous.setMotifConsultation(motif);
//				rendezvous.setPatient(patient);
//				rendezvous.setDay(c.getDay());
//				rendezvous.setStart(c.getStart());
//				rendezvous.setEnd(c.getEnd());
//				rendezvous.setStatut(c.getStatut());
//				rendezvous.setCanceledAt(c.getCanceledat());
//				rendezvous.setModeConsultation(mode);
//				rendezvous.setISnewPatient(c.getIsnewpatient());
//
//				if (user != null) {
//					ERole userRole = user.getRole();
//					if (userRole == ERole.ROLE_MEDECIN && c != null && c.getCommantaire() != null) {
//						rendezvous.setCommantaire(c.getCommantaire());
//					}
//				}
//
//				rendezvous.setLinkVideoCall(urlVideoCallGenerator.joinConference());
//				rendezvous.setCabinet(cabinet);
//
//				entityManger.persist(rendezvous);
//				ActivityServices.createActivity(new Date(), "Add", "Add New Rdv ", user);
//				LOGGER.info("Add new Rdv, UserID : " + user.getId());
//
//				return ResponseEntity.ok(mapper.map(rendezvous, Rendezvous.class));
//			} else {
//				return ResponseEntity.ok(new ApiResponse(false, "You already have another appointment with the same specialty on the same day."));
//			}
//		} catch (Exception e) {
//			throw new Exception(e.getMessage());
//		}
//	}


	// Checking if a patient has a rendezvous with a doctor today.
	public Boolean isHasRdvToday(Long spec,LocalDate date) throws Exception{
		try {
			List<Rendezvous> rdv= rdvrepo.findRdvBySpecInDate(spec, date);

			if(rdv.isEmpty()){
				return false;
			} else return true;
		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}

	}

	public RendezvousDTO getRdvToday(Long spec,LocalDate date) throws Exception{
		try {
			List<Rendezvous> rdvs= rdvrepo.findRdvBySpecInDate(spec, date);
			RendezvousDTO rdto=new RendezvousDTO();
			for(Rendezvous rdv : rdvs){
				if(rdv!=null){
					return convertToDTO(rdv,rdto);
				}
			}
			return null;
		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception(e.getMessage());
		}

	}


	public int getStatisticsByMed(LocalDate date, long idmed) throws Exception {
		return rdvrepo.findRdvByMedcinInDate(date, idmed);
	}
	public int getMonthlyStatisticsByMed(int year, int month, long idmed) throws Exception {
		return rdvrepo.findRdvByMedcinInMonth(year, month, idmed);
	}
	public int getTotalPatientsByMed(long idmed) throws Exception {
		return rdvrepo.findPatientsByMedcin(idmed);
	}

	public List<Rendezvous> getRendezVousByMed(long idmed){
	List<Rendezvous> rdvs= rdvrepo.findByMedecinIdAndStartAfterOrderByStartAsc(idmed, LocalDateTime.now());

	return rdvs;
	}
//	public List<RendezvousDTO> getRendezVousByMed(long idmed) {
//		List<Rendezvous> rdvs = rdvrepo.findByMedecinId(idmed);
//		List<RendezvousDTO> rdvDtos = new ArrayList<>();
//
//		for (Rendezvous rdv : rdvs) {
//			if (rdv != null) {
//				RendezvousDTO rdto = new RendezvousDTO();
//				rdvDtos.add(convertToDTO(rdv, rdto));
//				Patient patient=patientRepo.getById(rdto.getPatientid());
//			}
//		}
//
//		return rdvDtos;
//	}



}

