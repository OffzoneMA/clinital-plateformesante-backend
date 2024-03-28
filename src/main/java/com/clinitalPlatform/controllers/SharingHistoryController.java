package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.SharingHistory;
import com.clinitalPlatform.payload.request.SharingHistoryRequest;
import com.clinitalPlatform.services.SharingHistoryServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shares")
public class SharingHistoryController {

    @Autowired
    public SharingHistoryServiceImpl shareservices;


    @PostMapping("/addshare")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> AddShare(@Valid @RequestBody SharingHistoryRequest sharingHistoryRequest) throws Exception{
        SharingHistory shareHistory= shareservices.addSharingHistory(sharingHistoryRequest);
        return ResponseEntity.ok(shareHistory);
    }

    @PutMapping("/updateshare")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> UpdateShare(@Valid @RequestBody SharingHistoryRequest sharingHistoryRequest) throws Exception{
        return ResponseEntity.ok(shareservices.updateSharingHistory(sharingHistoryRequest));
    }

    @DeleteMapping("/deleteteshare/{idshare}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> DeleteShare(@Valid @PathVariable Long idshare) throws Exception{
        return ResponseEntity.ok(shareservices.deleteSharingHistoryById(idshare));
    }

    // get shared history between patient and doctors
    @GetMapping("/Medecins")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public ResponseEntity<?> MedecinSharingHistory(@RequestParam Long idpat, @RequestParam Long id_med) throws Exception{
        return ResponseEntity.ok(shareservices.findAllSharingHistoryByMedecinIdAndPatientId(id_med, idpat));
    }

    // get history of sharing a document.
    @GetMapping("/dossiers/{iddoss}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public ResponseEntity<?> documentSharingHistory(@PathVariable("iddoss") Long id) throws Exception{
        return ResponseEntity.ok(shareservices.findAllSharingHistoryByDocId(id));
    }

    // get history of sharing of a document between current user and Medecin.
    @GetMapping("/users/{idmed}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public ResponseEntity<?> UserandMedSharingHistory(@PathVariable("idmed") Long idmed) throws Exception{
        return ResponseEntity.ok(shareservices.findAllSharingHistoryByMedecinIdAndUserId(idmed));
    }

    // get history of sharing of a document between a patient and Medecin.
    @GetMapping("/Patients/{idpat}/{idmed}")
    @ResponseBody
    public ResponseEntity<?> PatientandMedSharingHistory(@PathVariable("idpat") Long idpat,@PathVariable("idmed") Long idmed) throws Exception{
        return ResponseEntity.ok(shareservices.findAllSharingHistoryByMedecinIdAndPatientId(idmed,idpat));
    }

}
