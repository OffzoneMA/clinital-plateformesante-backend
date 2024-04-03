package com.clinitalPlatform.services;

import com.clinitalPlatform.dao.IDao;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.repository.DocumentRepository;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Transactional
@Service
@Primary
public class PatientService implements IDao<Patient> {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DossierMedicalRepository dossierMedicalRepository;
	
	@Autowired
    private ClinitalModelMapper modelMapper;

	@Autowired
	private MedecinRepository medRepository; 

//	@Autowired
//	private AntecedentsRepository antRepository;
	@Autowired
	private RendezvousService rendezvousService;
	@Autowired
	private ActivityServices ActivityServices;
	@Autowired
	private DocumentRepository documentRepository;
	@Autowired
	private GlobalVariables globalVariables;
	private final Logger LOGGER=LoggerFactory.getLogger(getClass());

	@Override
	public Patient create(Patient user){
		try {
		DossierMedical dossierMedical = new DossierMedical();
			dossierMedical.setAlchole(false);
			dossierMedical.setFumeur(false);
			dossierMedical.setAccesscode(null);
			dossierMedical.setDossierType(user.getPatient_type());
			dossierMedical.setNumDossier(null);
			dossierMedical.setTraitement(true);
			dossierMedicalRepository.save(dossierMedical);
			user.setDossierMedical(dossierMedical);
		// save activity update Patient 
		ActivityServices.createActivity(new Date(), "ADD", "Add New Patient", globalVariables.getConnectedUser());
		
			LOGGER.info("Add new Patient "+user.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return patientRepository.save((Patient) user);
		

		
		

	}

	@Override
	public void update(Patient o) {
		patientRepository.save(o);
	}

	@Override
	public void delete(Patient o) {
		patientRepository.deletePatient(o.getId());;
	}

	@Override
	public List<Patient> findAll() {
		return patientRepository.findAll();
	}

	@Override
	public Optional<Patient> findById(long id) {
		return patientRepository.findById(id);
	}
	
	/**
     * It returns a Patient object from the database, where the user_id is equal to the id parameter, and
     * the patient_type is equal to the string "MOI" it mean the current USER connected
     *
     * @param id the id of the user
     * @return Patient
     */
	
	public Patient getPatientMoiByUserId(long id){
		
		return patientRepository.getPatientMoiByUserId(id);

	}

	/**
	 * I want to find a patient by his id and his user_id and his patient_type
	 * 
	 * @param id the id of the user
	 * @param idpatient the id of the patient
	 * @return A list of patients
	 */
	
	public ResponseEntity<Patient> findProchByUserId(long id,long idpatient){

		Patient patient = patientRepository.findProchByUserId(id, idpatient);
		return ResponseEntity.ok(modelMapper.map(patient,Patient.class));
	}

	/**
	 * It returns a list of patients that are related to the user with the id passed as a parameter
	 * where type is PROCHE
	 * 
	 * @param id the id of the user
	 * @return List of Patient
	 */
	
	public List<Patient> findALLProchByUserId(long id){

		return patientRepository.findALLProchByUserId(id).stream()
		.map(pat -> modelMapper.map(pat, Patient.class)).collect(Collectors.toList());

	}

	/**
	 * It returns a list of patients that have the same user_id as the id passed in
	 * 
	 * @param id the id of the user
	 * @return List of Patient objects
	 */
	
	public List<Patient> findALLPatientByUserId(long id){

		return patientRepository.findALLPatientByUserId(id).stream()
		.map(pat -> modelMapper.map(pat, Patient.class)).collect(Collectors.toList());

	}

// share a folder by patient to a specifique doctor : 
	public ResponseEntity<?> ShareMedecialFolder(Long iddossier,Long idmed) throws Exception{

		Medecin med = medRepository.findById(idmed).orElseThrow(()->new Exception("NO such Medecin exist"));
		DossierMedical dossier = dossierMedicalRepository.findById(iddossier).orElseThrow(()->new Exception("NO such Folder exist"));



		// check if the folder is already shared.
		Boolean isDossshared=med.getMeddossiers().stream().filter(doss->doss.getId_dossier()==dossier.getId_dossier()).findFirst().isPresent();

		//boolean isDossshared = med.getMeddossiers().stream().anyMatch(o -> doss.getId_dossier()==dossier.getId_dossier());
		if(!isDossshared){
			med.getMeddossiers().add(dossier);
			medRepository.save(med);
			ResponseEntity.status(200).build();
		} else{
			return ResponseEntity.ok(new ApiResponse(false, "You already shared this folder with that doctor"));
		}
		

		return ResponseEntity.ok("Folder shared seccessefully !");
	}


//GENERATE FICHE PATIENT BY DOCTOR :
	
//public FichePatientResponse GenrateFichepatient(Long idpatient,Medecin med) throws Exception{
//
//	try {
//		// get patient
//		System.out.println(" get pateint");
//		Patient patient = patientRepository.findById(idpatient).orElseThrow(()->new Exception("Patient not found"));
//		// get Medical folder relited to this doctor and patient above :
//		// medecin.getMeddossiers().stream().filter(dossier->dossier.getId_dossier()==iddoss).findFirst().get();
//		System.out.println(" dossier medical"+patient.getDossierMedical().getId_dossier());
//		DossierMedical dossierMedical= dossierMedicalRepository.getdossierByIdandMedId(med.getId(), patient.getDossierMedical().getId_dossier()).orElseThrow(()->new Exception("No matching found"));
//		// get rdv for this patient and this doctor:
//		System.out.println(" get rdv for this pat with this med");
//		List<Rendezvous> listrdvpatient=rendezvousService.getRdvByIdMedecinandIdPatient(patient.getId(), med.getId()).stream().map(doc->modelMapper.map(doc, Rendezvous.class)).collect(Collectors.toList());
//
//		// get documents relited to dis folder:
//		System.out.println(" get documents from folder relited");
//		List<Document> listrddocument=documentRepository.findByIdDossier(dossierMedical.getId_dossier()).stream().map(doc->modelMapper.map(doc, Document.class)).collect(Collectors.toList());
//
//
//		//get All Antecedents relited to this folder :
//		List<Antecedents> allantecedents=antRepository.findAll()
//		.stream()
//		.filter(antecedents->antecedents.getDossier().getId_dossier()==dossierMedical.getId_dossier())
//		.collect(Collectors.toList());
//        // get list of all patients that are related to this doctor
//		System.out.println(" generate fiche");
//		FichePatientResponse fiche= new FichePatientResponse();
//
//		fiche.setId(patient.getId());
//		fiche.setNom_pat(patient.getNom_pat());
//		fiche.setPrenom_pat(patient.getPrenom_pat());
//		fiche.setCivilite_pat(patient.getCivilite_pat());
//		fiche.setAdresse_pat(patient.getAdresse_pat());
//		fiche.setDateNaissance(patient.getDateNaissance());
//		fiche.setCodePost_pat(patient.getCodePost_pat());
//		fiche.setMatricule_pat(patient.getMatricule_pat());
//		fiche.setMutuelNumber(patient.getMutuelNumber());
//		fiche.setPatientEmail(patient.getPatientEmail());
//		fiche.setPatientTelephone(patient.getPatientTelephone());
//		// get rdv
//		System.out.println(" get rdvs");
//		if(!listrdvpatient.isEmpty()){
//
//			for (Rendezvous rdv : listrdvpatient) {
//				fiche.getAllrdv().add(rdv);
//				}
//		}
//		// get Antecedents
//		System.out.println(" get Antecedents");
//		if(!allantecedents.isEmpty()){
//
//			for (Antecedents Antecedents : allantecedents) {
//				fiche.getAllantecedents().add(Antecedents);
//				}
//		}
//		//get docs:
//		System.out.println(" get docs");
//		if(!listrddocument.isEmpty()){
//// how to add now element to a list in for loop ?
//				for (Document doc : listrddocument) {
//						fiche.getAlldoc().add(doc);
//					}
//			}
//
//		return fiche;
//
//
//	} catch (Exception e) {
//		// TODO: handle exception
//		throw new Exception(e.getMessage());
//	}
//
//
//}

//GENERATE FICHE PATIENT :

//public FichePatientResponse Fichepatient(Long idpatient,long userid) throws Exception{
//
//	try {
//
//		// get patient
//		Patient patient = patientRepository.findALLPatientByUserId(userid).stream().filter(pat->pat.getUser().getId()==userid && pat.getId()==idpatient ).findFirst().orElseThrow(()->new Exception("Patient not found"));
//		// get Medical folder relited to this doctor and patient above :
//		// medecin.getMeddossiers().stream().filter(dossier->dossier.getId_dossier()==iddoss).findFirst().get();
//		DossierMedical dossierMedical= dossierMedicalRepository.findAll().stream().filter(doss->doss.getId_dossier()==patient.getDossierMedical().getId_dossier()).findFirst().orElseThrow(()->new Exception("No matching found"));
//		// get rdv for this patient and this doctor:
//		List<Rendezvous> listrdvpatient=rendezvousService.findRdvByIdUserandPatient(patient.getUser().getId(), patient.getId());
//
//		// get documents relited to dis folder:
//		List<Document> listrddocument=documentRepository.findByDossier(dossierMedical);
//
//        // get list of all patients that are related to this doctor
//
//		FichePatientResponse fiche= new FichePatientResponse();
//
//		fiche.setId(patient.getId());
//		fiche.setNom_pat(patient.getNom_pat());
//		fiche.setPrenom_pat(patient.getPrenom_pat());
//		fiche.setCivilite_pat(patient.getCivilite_pat());
//		fiche.setAdresse_pat(patient.getAdresse_pat());
//		fiche.setDateNaissance(patient.getDateNaissance());
//		fiche.setCodePost_pat(patient.getCodePost_pat());
//		fiche.setMatricule_pat(patient.getMatricule_pat());
//		fiche.setMutuelNumber(patient.getMutuelNumber());
//		fiche.setPatientEmail(patient.getPatientEmail());
//		fiche.setPatientTelephone(patient.getPatientTelephone());
//
//		if(!listrdvpatient.isEmpty()){
//
//			for (Rendezvous rdv : listrdvpatient) {
//				fiche.getAllrdv().add(rdv);
//				}
//		}
//		if(!listrddocument.isEmpty()){
//
//				for (Document doc : listrddocument) {
//						fiche.getAlldoc().add(doc);
//					}
//			}
//
//		return fiche;
//
//
//	} catch (Exception e) {
//		// TODO: handle exception
//		throw new Exception(e.getMessage());
//	}
//
//
//}

}
