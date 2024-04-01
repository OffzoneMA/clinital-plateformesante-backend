package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.SharingHistoryDTO;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.SharingHistoryRequest;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.services.interfaces.SharingHistoryService;

import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SharingHistoryServiceImpl implements SharingHistoryService {

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private sharingHistoryrepository sharinghistoryRepository ;

    @Autowired
    private UserRepository userrepo;

    @Autowired
    private PatientRepository patientrepo;

    @Autowired
    private DossierMedicalRepository dossrepo;

    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private ClinitalModelMapper mapper;


    @Override
    public SharingHistory addSharingHistory(SharingHistoryRequest sharingHistoryRequest) throws Exception {
        try {

            Medecin medecin= medecinRepository.findById(sharingHistoryRequest.getMedecinId()).orElseThrow(()->new Exception("No Medecin exist with this ID"));
            Patient patient= patientrepo.findById(sharingHistoryRequest.getPatientId()).orElseThrow(()->new Exception("No Patient exist with this ID"));

            DossierMedical dossierMedical= dossrepo.findById(sharingHistoryRequest.getDossierId()).orElseThrow(()->new Exception("No Folder exist with this ID"));
            User currentUser= globalVariables.getConnectedUser();

            SharingHistory newShare= new SharingHistory(currentUser, medecin, patient, dossierMedical, LocalDateTime.now());
            sharinghistoryRepository.save(newShare);

            return newShare;

        } catch (Exception e) {

            throw new Exception(e.getMessage());
        }

    }

    @Override
    public ResponseEntity<SharingHistory> updateSharingHistory(SharingHistoryRequest sharingHistoryRequest)
            throws Exception {
        try {

            SharingHistory shareHistory=sharinghistoryRepository.findById(sharingHistoryRequest.getId()).orElseThrow(()->new Exception("No sharing history exists with this ID"));
            Medecin medecin=medecinRepository.findById(sharingHistoryRequest.getMedecinId()).orElseThrow(()->new Exception("No Medecin exists with this ID"));
            Patient patient=patientrepo.findById(sharingHistoryRequest.getPatientId()).orElseThrow(()->new Exception("No Patient exists with this ID"));
            DossierMedical dossierMedical=dossrepo.findById(sharingHistoryRequest.getDossierId()).orElseThrow(()->new Exception("No Folder exists with this ID"));

            User currentUser=globalVariables.getConnectedUser();

            shareHistory.setMedecin(medecin);
            shareHistory.setPatient(patient);
            shareHistory.setDossierMedical(dossierMedical);
            shareHistory.setUser(currentUser);
            shareHistory.setDateshare(sharingHistoryRequest.getSharingdate());
            sharinghistoryRepository.save(shareHistory);

            return ResponseEntity.ok(shareHistory);

        } catch (Exception e) {

            throw new Exception(e.getMessage());
        }


    }

    @Override
    public String deleteSharingHistoryById(Long id) throws Exception {
        try {

            if(id == null){
                return "ID is NULL !!";
            }
            sharinghistoryRepository.deleteById(id);

            return "record has been deleted seccessfully";

        } catch (Exception e) {

            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<SharingHistory> findAllSharingHistoryByMedecinIdAndPatientId(Long id_medecin, Long id_patient)
            throws Exception {
        try {
            Patient patient=patientrepo.findById(id_patient).orElseThrow(()->new Exception("No Patient Found with this ID"));
            Medecin medecin=medecinRepository.findById(id_medecin).orElseThrow(()->new Exception("No Medecin Found with this ID"));

            List<SharingHistory> shares=sharinghistoryRepository.findAllSharingHistoryByMedecinIdAndPatientId(medecin.getId(), patient.getId());

            return shares;


        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<SharingHistory> findAllSharingHistoryByPatientId(Long id_patient) throws Exception {
        try {
            Patient patient=patientrepo.findById(id_patient).orElseThrow(()->new Exception("No matchinf Found for this Patient ID"));
            List<SharingHistory> shares=sharinghistoryRepository.findAllSharingHistoryByPatientId(patient.getId())
                    .stream()
                    .collect(Collectors.toList());
            return shares;

        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }


    @Override
    public List<Medecin> findAllSharingHistoryByMedecinId(Long id_med) throws Exception {
        try {

            Medecin medecin=medecinRepository.findById(id_med).orElseThrow(()->new Exception("No Medecin Found for this ID"));
            List<Medecin> medecins=sharinghistoryRepository.findAllSharingHistoryByMedecinId(medecin.getId())
                    .stream()
                    .collect(Collectors.toList());
            return medecins;
        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<SharingHistory> findAllSharingHistoryByMedecinIdAndUserId(Long id_medecin)
            throws Exception {
        try {

            Medecin medecin=medecinRepository.findById(id_medecin).orElseThrow(()->new Exception("No Medecin Found with this ID"));


            List<SharingHistory> shares=sharinghistoryRepository.findAllSharingHistoryByMedecinIdAndUserId(medecin.getId(),globalVariables.getConnectedUser().getId());

            return shares;
        } catch (Exception e) {

            throw new Exception(e.getMessage());
        }

    }

    @Override
    public ResponseEntity<?> findAllSharingHistoryByUserId(Long user_id) throws Exception {
        try {


            List<SharingHistory> shares=sharinghistoryRepository.findAllSharingHistoryByUserId(globalVariables.getConnectedUser().getId())
                    .stream()
                    .map(share->mapper.map(share, SharingHistory.class)).collect(Collectors.toList());
            if(shares.isEmpty()){
                return ResponseEntity.ok("Your history is empty");
            }
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<SharingHistory> findAllSharingHistoryByDocId(Long id_doss) throws Exception {
        try {

            DossierMedical dossierMedical=dossrepo.findById(id_doss).orElseThrow(()->new Exception("No Folder exist with this id"));
            List<SharingHistory> shares=sharinghistoryRepository.findAllSharingHistoryByDossierId(dossierMedical.getId_dossier());

            return shares;

        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public SharingHistory findSharingHistoryById(Long id_share) throws Exception {
        try {

            SharingHistory share=sharinghistoryRepository.findById(id_share).orElseThrow(()->new Exception("No matchinf Found for this History sharing ID"));
            return share;
        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }

}
