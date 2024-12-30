package com.clinitalPlatform.services;

import com.clinitalPlatform.dao.IDao;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.repository.*;
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
	private MedecinRepository medRepository;
  
	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DossierMedicalRepository dossierMedicalRepository;
	
	@Autowired
    private ClinitalModelMapper modelMapper;

	@Autowired
	private RdvRepository rdvRepository ;

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
		Logger logger = LoggerFactory.getLogger(this.getClass());

		try {
			// Vérifier si le patient a des rendez-vous à venir ou confirmés
			if (rdvRepository.countPendingOrUpcomingRendezvous(o.getId()) > 0) {
				throw new IllegalStateException("Vous ne pouvez pas supprimer ce patient car il a un rendez-vous en attente ou confirmé.");
			}

			// Supprimer les associations dans DocumentMedecin basées sur les rendez-vous
			documentRepository.deleteDocumentsMedecinsByRendezvousPatient(o.getId());

			// Supprimer les associations dans DocumentMedecin basées sur le patient
			documentRepository.deleteDocumentsMedecinsByPatientId(o.getId());

			// Supprimer les documents associés aux rendez-vous du patient
			documentRepository.deleteDocumentsByPatient(o.getId());
			documentRepository.deleteDocumentsByPatientId(o.getId());

			// Supprimer les rendez-vous associés au patient
			patientRepository.deleteRendezvousByPatient(o.getId());

			// Supprimer le patient
			patientRepository.deletePatient(o.getId());

		} catch (IllegalStateException e) {
			logger.error("Erreur de suppression du patient {} : {}", o.getId(), e.getMessage());
			throw e; // Relance l'exception pour signaler l'erreur à l'appelant

		} catch (Exception e) {
			logger.error("Une erreur inattendue s'est produite lors de la suppression du patient {}.", o.getId(), e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public List<Patient> findAll() {
		return patientRepository.findAll();
	}

	@Override
	public Optional<Patient> findById(long id) {
		return patientRepository.findById(id);
	}
	
	public Patient getPatientMoiByUserId(long id){
		
		return patientRepository.getPatientMoiByUserId(id);

	}

	public ResponseEntity<Patient> findProchByUserId(long id,long idpatient){

		Patient patient = patientRepository.findProchByUserId(id, idpatient);
		return ResponseEntity.ok(modelMapper.map(patient,Patient.class));
	}

	

	public List<Patient> findALLProchByUserId(long id){

		return patientRepository.findALLProchByUserId(id).stream()
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

	public void setUserNullByUserId(Long userId) {
	    // Récupérer tous les patients associés à l'utilisateur ayant user_id=id
	    List<Patient> patientsToUpdate = patientRepository.findByUserId(userId);
	    
	    // Définir la propriété user sur null pour chaque patient récupéré
	    for (Patient patient : patientsToUpdate) {
	        patient.setUser(null);
	    }
	    
	    // Enregistrer les changements
	    patientRepository.saveAll(patientsToUpdate);
	}

	public List<Patient> findALLPatientByUserId(long id){

		return patientRepository.findALLPatientByUserId(id).stream()
		.map(pat -> modelMapper.map(pat, Patient.class)).collect(Collectors.toList());

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
