package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.MedecinDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.MedecinRequest;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.repository.VilleRepository;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import com.clinitalPlatform.services.interfaces.MedecinService;
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

//    @Autowired
//    private PatientRepository patientRepository;

//    @Autowired
//    RendezvousService rendezvousService;

//    @Autowired
//    PatientService patientService;

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
    VilleRepository villeRepository;
//    @Autowired
//    DipolmeMedecinRepository diplomerepo;
//    @Autowired
//    CabinetRepository cabinetrepo;
//    @Autowired
//    CabinetMedecinRepository cabmedrepo;
    private final Logger LOGGER= LoggerFactory.getLogger(getClass());

    @Override
    public MedecinDTO create(MedecinRequest request) throws Exception {
//        try {
//
//            //UserDetailsImpl ConnectedUser=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            Cabinet cabinet=cabinetrepo.findById(request.getCabinet()).orElseThrow(()->new Exception("No Matching Cabinet"));
//            Medecin med=medecinRepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
//            Boolean IsAllowedtoAdd=CabinetMedecinRepository.isAllowed(med.getId(), cabinet.getId_cabinet());
//
//            if(IsAllowedtoAdd){
//
     Medecin medecin = medecinRepository.findById(request.getId()).orElseThrow(()->new Exception("No Matching Medecin"));
//                CabinetMedecinsSpace medcab=new CabinetMedecinsSpace();
//                medcab.setMedecin(medecin);
//                medcab.setCabinet(cabinet);
//                medcab.setStatus(CabinetStatuMedcinEnum.USER);
//                cabmedrepo.save(medcab);
//                cabinet.getMedecin().add(medcab);
//                cabinetrepo.save(cabinet);
//
//                activityServices.createActivity(new Date(),"Add","Add New Medecin By Medecin Admin to cabinet ID :"+cabinet.getId_cabinet(),globalVariables.getConnectedUser());
//                LOGGER.info("Add New Medecin By Medecin Admin to cabinet ID :"+cabinet.getId_cabinet()+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//
          return clinitalModelMapper.map(medecin, MedecinDTO.class);
//            } else
//                LOGGER.info("You are not Allowed to add new Medecin, User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//            throw new Exception("Your are not Allowed");
//
//        } catch (Exception e) {
//            throw new Exception(e.getMessage());
//        }





    }

    @Override
    public MedecinDTO update(MedecinRequest request, Long id) throws Exception {
        try {
            UserDetailsImpl ConnectedUser=(UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user= userRepo.getById(ConnectedUser.getId());
            Medecin medecin=medecinRepository.getMedecinByUserId(ConnectedUser.getId());
           // Ville ville=VilleRepository.findById(request.getVille()).orElseThrow(()->new Exception("No Matching Ville"));
           // DiplomeMedecin diplome=diplomerepo.findById(request.getDiplome_med()).orElseThrow(()->new Exception("No Matching Diplome"));

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
            //medecin.setVille(ville);
           // medecin.getDiplome_med().add(diplome);
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
        Medecin med = medecinRepository.findById(id).orElseThrow(() -> new Exception("Medecin not found"));
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

//    @Override
//    public List<PatientDTO> getMedecinPatients(Long id) throws Exception {
//        Medecin med = medecinRepository.getMedecinByUserId(id);
//        return patientRepository.getMedecinPatients(med.getId()).stream()
//                .map(patient -> clinitalModelMapper.map(patient, PatientDTO.class)).collect(Collectors.toList());
//    }
//
//    @Override
//    public PatientDTO getPatient(Long iduser, long idpat) throws Exception {
//        Patient pat = patientRepository.findById(idpat).orElseThrow(() -> new Exception("Patient not found"));
//        Medecin med = medecinRepository.getMedecinByUserId(iduser);
//        Patient patient = patientRepository.getPatient(pat.getId(), med.getId());
//        return clinitalModelMapper.map(patient, PatientDTO.class);
//    }

    @Override
    public Medecin getMedecinByUserId(long id) throws Exception {

        Medecin med = medecinRepository.getMedecinByUserId(id);

        return clinitalModelMapper.map(med, Medecin.class);
    }

    // Creating a creno.


    // It's returning the number of days in a month.

    // Setting the visibility of the medecin.

    // Send request to Ptient to grant docotr persmession to access his folder.

    // Access to Folder of a patient


// Uplaod profile picture of a doctor :

    // my visibility






}

