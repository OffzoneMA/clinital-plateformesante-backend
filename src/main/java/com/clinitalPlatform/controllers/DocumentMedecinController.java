package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Document;
import com.clinitalPlatform.models.DocumentMedecin;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.repository.DocumentMedecinRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.DocumentMedecinService;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.util.GlobalVariables;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/documents-medecin")
public class DocumentMedecinController {

    @Autowired
    private DocumentMedecinService documentMedecinService;
    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private MedecinServiceImpl medecinServiceImpl;
    @Autowired
    private DocumentMedecinRepository documentMedecinRepository;

    @Autowired
    private ActivityServices activityServices;
    @Autowired
    private MedecinRepository medecinRepository;

    @PostMapping(path = "/add_Doc/by-medecin")
    @PreAuthorize("hasAuthority('ROLE_MEDECIN')")
    @ResponseBody
    public ResponseEntity<?> addDocByMedecin(@RequestParam String document,
                                             @RequestParam MultipartFile docFile) throws Exception {
        try {

            Long userId = globalVariables.getConnectedUser().getId();

            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userId);

            if (medecin == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Medecin not found!"));
            }
            // Add MedecinId to the document String
            document = document.replace("}", ",\"medecinId\":" + medecin.getId() + "}");

            // ------ Save Doc
            DocumentMedecin savedDoc = documentMedecinService.create(document);

            // --------------------- Initial doc Upload
            String extension = FilenameUtils.getExtension(docFile.getOriginalFilename());
            String fileName = savedDoc.getTitre_doc()+ "." + extension;

            // --------------- return file name after Uploading
            //String UploadedFile = azureAdapter.upload(docFile,fileName,"Patientdoc");
            String uploadedFile = "this is the URL of uploaded file: "+fileName+" in azure";//just for test until we have azure account

            savedDoc.setFichier_doc(uploadedFile);

            savedDoc.setNumero_doc(savedDoc.getId_doc());//Numero doc is doc id
            // --------------- update saved doc
            DocumentMedecin finalSavedDoc = documentMedecinRepository.save(savedDoc);

            activityServices.createActivity(new Date(),"Add","Add New document ID:"+finalSavedDoc.getId_doc(),globalVariables.getConnectedUser());
            return ResponseEntity.ok(new ApiResponse(true, "Document Medecin created successfully!",finalSavedDoc));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse(false, "Document not created!"+e.getMessage()));

        }
    }

    @PutMapping("/{id}")
    public DocumentMedecin update(@PathVariable Long id, @RequestBody DocumentMedecin doc) {
        doc.setId_doc(id);
        return documentMedecinService.save(doc);
    }

    @GetMapping("/{id}")
    public DocumentMedecin getById(@PathVariable Long id) {
        return documentMedecinService.getById(id).orElse(null);
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<?> getByMedecin(@PathVariable Long medecinId) {
        try {
            Medecin medecin = medecinRepository.findById(medecinId).orElse(null);

            if (medecin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Medecin not found!"));
            }
            List<DocumentMedecin> documents = documentMedecinRepository.findByMedecinAuteurId(medecinId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving documents: " + e.getMessage()));
        }
    }

    @GetMapping("/medecin/connected")
    @PreAuthorize("hasAuthority('ROLE_MEDECIN')")
    public ResponseEntity<?> getByConnectedMedecin() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userId);

            if (medecin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Medecin not found!"));
            }
            List<DocumentMedecin> documents = documentMedecinRepository.findByMedecinAuteurId(medecin.getId());
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving documents: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        documentMedecinService.delete(id);
    }

    @PostMapping("/share-documents-to-medecin")
    @PreAuthorize("hasAnyRole('ROLE_MEDECIN' , 'ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<?> shareDocumentsWithMedecin( @RequestParam Long medecinId, @RequestBody List<Long> documentIds) {

        // Validation des entrées
        if (medecinId == null) {
            return ResponseEntity.badRequest().body("Medecin ID is required");
        }
        if (documentIds == null || documentIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Document IDs list is required and cannot be empty");
        }

        try {
            Long userId = globalVariables.getConnectedUser().getId();

            // Appeler le service pour partager les documents
            Medecin medecin = documentMedecinService.shareDocumentMedWithMedecin(medecinId, documentIds , userId);

            // Retourner une réponse descriptive
            return ResponseEntity.ok(new ApiResponse(true, "Documents shared successfully!"));
        } catch (IllegalArgumentException e) {
            // Gérer les erreurs causées par des IDs invalides
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Gérer les erreurs inattendues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/share-documents-to-patient")
    @PreAuthorize("hasAnyRole('ROLE_MEDECIN' , 'ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<?> shareDocumentsWithPatient( @RequestParam Long patientId, @RequestBody List<Long> documentIds) {

        // Validation des entrées
        if (patientId == null) {
            return ResponseEntity.badRequest().body("Medecin ID is required");
        }
        if (documentIds == null || documentIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Document IDs list is required and cannot be empty");
        }

        try {
            Long userId = globalVariables.getConnectedUser().getId();
            // Appeler le service pour partager les documents
            Patient patient = documentMedecinService.shareDocumentMedWithPatient(patientId, documentIds , userId );

            // Retourner une réponse descriptive
            return ResponseEntity.ok(new ApiResponse(true, "Documents shared successfully!"));
        } catch (IllegalArgumentException e) {
            // Gérer les erreurs causées par des IDs invalides
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Gérer les erreurs inattendues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/shared-documents-by-medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ROLE_MEDECIN' , 'ROLE_ADMIN')")
    public ResponseEntity<?> getSharedDocumentsByMedecin(@PathVariable Long medecinId) {
        try {
            List<DocumentMedecin> sharedDocuments = documentMedecinService.getSharedDocumentsByMedecin(medecinId);
            return ResponseEntity.ok(sharedDocuments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving shared documents: " + e.getMessage()));
        }
    }

    // Get all docs shared with one medecin
    @GetMapping("/shared-documents/with-medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ROLE_MEDECIN' , 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllSharedDocumentsWithMedecin(@PathVariable Long medecinId) {
        try {
            List<DocumentMedecin> sharedDocuments = documentMedecinService.getSharedDocumentsWithMedecin(medecinId);
            return ResponseEntity.ok(sharedDocuments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving shared documents: " + e.getMessage()));
        }
    }

    @GetMapping("/shared-documents/with-patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_PATIENT' , 'ROLE_ADMIN' , 'ROLE_MEDECIN')")
    public ResponseEntity<?> getAllSharedDocumentsWithPatient(@PathVariable Long patientId) {
        try {
            List<DocumentMedecin> sharedDocuments = documentMedecinService.getSharedDocumentsWithPatient(patientId);
            return ResponseEntity.ok(sharedDocuments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving shared documents: " + e.getMessage()));
        }
    }

    @GetMapping("/shared-documents/with-patient/connected")
    @PreAuthorize("hasAnyRole('ROLE_PATIENT')")
    public ResponseEntity<?> getAllSharedDocumentsWithPatientConnected() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            List<DocumentMedecin> sharedDocuments = documentMedecinService.getSharedDocumentsWithPatientAndProches(userId);
            return ResponseEntity.ok(sharedDocuments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving shared documents: " + e.getMessage()));
        }
    }


}
