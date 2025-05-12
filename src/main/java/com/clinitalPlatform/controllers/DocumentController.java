package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.DocumentDTO;
import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.UpdateDocumentRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.DocumentResponse;
import com.clinitalPlatform.payload.response.TypeDocumentResponse;
import com.clinitalPlatform.repository.DocumentRepository;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.repository.RdvRepository;
import com.clinitalPlatform.repository.TypeDocumentRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.DocumentPatientServices;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
//import com.clinitalPlatform.security.config.azure.AzureServices;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doc")
public class DocumentController {

    @Autowired
    private DocumentRepository docrepository;

    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private RdvRepository rdvRepository;

    @Autowired
    private DocumentPatientServices docservices;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private TypeDocumentRepository typeDocumentRepo;

    @Autowired
    private ClinitalModelMapper mapper;

    //@Autowired
    //private AzureServices azureAdapter;

    @Autowired
    private ActivityServices activityServices;

    private final Logger LOGGER= LoggerFactory.getLogger(getClass());

    // Returning a list of documents.
    @GetMapping("/documents")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    Iterable<DocumentResponse> documents() throws Exception {
        Optional<Patient> patient = patientRepo.findByUserIdAndPatientType(globalVariables.getConnectedUser().getId());
        // Verify if the list of patient is empty
        if (!patient.isPresent()) {
            return Collections.emptyList();
        } else {

            activityServices.createActivity(new Date(), "Read", "Consulting all Documents", globalVariables.getConnectedUser());
            LOGGER.info("Consulting All documents By User ID : {}", globalVariables.getConnectedUser() != null ? globalVariables.getConnectedUser().getId() : "");
            return docrepository.getMyDocs(globalVariables.getConnectedUser().getId())
                    .stream().map(document -> mapper.map(document, DocumentResponse.class)).collect(Collectors.toList());
        }
    }

    @GetMapping(path = "/patientsConcerned")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    Iterable<Patient> getPatientsConcerned() throws Exception {

        Optional<Patient> patient = patientRepo.findByUserIdAndPatientType(globalVariables.getConnectedUser().getId());

        if (patient.isEmpty()) {
            return Collections.emptyList();
        }else {

            activityServices.createActivity(new Date(), "Read", "Consulting all patients concerned", globalVariables.getConnectedUser());
            LOGGER.info("Consulting All patients concerned By User ID : " + (globalVariables.getConnectedUser() != null ? globalVariables.getConnectedUser().getId() : ""));
            System.out.println("TEST ici"+globalVariables.getConnectedUser());


            return new ArrayList<>(docrepository.getMeAndMesProches(globalVariables.getConnectedUser().getId()));
        }

    }

    // Add a document of a patient
    @PostMapping(path = "/addDoc")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public ResponseEntity<?> addDoc(@RequestParam String document,
                                    @RequestParam MultipartFile docFile) throws Exception {

        try {

            // ------ Save Doc
            Document savedDoc = docservices.create(document);

            // --------------------- Initial doc Upload
            String extension = FilenameUtils.getExtension(docFile.getOriginalFilename());
            String fileName = savedDoc.getTitre_doc()+ "." + extension;

            // --------------- return file name after Uploading
            //String UploadedFile = azureAdapter.upload(docFile,fileName,"Patientdoc");
            String uploadedFile = "this is the URL of uploaded file: "+fileName+" in azure";//just for test until we have azure account

            savedDoc.setFichier_doc(uploadedFile);

            savedDoc.setNumero_doc(savedDoc.getId_doc());//Numero doc is doc id

            // --------------- update saved doc
            Document finalSavedDoc = docrepository.save(savedDoc);
            // ----- add doc info to RDV :
            //Rendezvous rdv= rdvRepository.findById(finalSavedDoc.getRendezvous().getId()).orElseThrow(()->new Exception("NO MATCHING FOUND"));
            //rdv.getDocuments().add(finalSavedDoc);

            //update rendez-vous
            //rdvRepository.save(rdv);

            activityServices.createActivity(new Date(),"Add","Add New document ID:"+finalSavedDoc.getId_doc(),globalVariables.getConnectedUser());
            LOGGER.info("Add new document with ID "+finalSavedDoc.getId_doc()+" By User with ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
            return ResponseEntity.ok(new ApiResponse(true, "Document created successfully!",finalSavedDoc));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse(false, "Document not created!"+e.getMessage()));

        }
    }

    @PostMapping(path = "/addAndShareDoc")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public ResponseEntity<?> addAndShareDoc(
            @RequestParam String document,
            @RequestParam MultipartFile docFile,
            @RequestParam Long medecinId) throws Exception {

        try {
            // ------ Save Document
            Document savedDoc = docservices.create(document);

            // ------ Handle File Upload
            String extension = FilenameUtils.getExtension(docFile.getOriginalFilename());
            String fileName = savedDoc.getTitre_doc() + "." + extension;

            // Simuler l'upload du fichier (remplacez par votre implémentation Azure)
            String uploadedFileUrl = "this is the URL of uploaded file: " + fileName + " in Azure"; // Placeholder
            savedDoc.setFichier_doc(uploadedFileUrl);

            // ------ Mettre à jour les métadonnées du document
            savedDoc.setNumero_doc(savedDoc.getId_doc()); // Numero doc is doc ID

            // ------ Sauvegarder les modifications du document
            Document finalSavedDoc = docrepository.save(savedDoc);

            //Rendezvous rdv= rdvRepository.findById(finalSavedDoc.getRendezvous().getId()).orElseThrow(()->new Exception("NO MATCHING FOUND"));
            //rdv.getDocuments().add(finalSavedDoc);

            // ------ Partager le document avec le médecin
            List<Long> documentIds = Collections.singletonList(finalSavedDoc.getId_doc());
            docservices.shareDocumentsWithMedecin(medecinId, documentIds);

            // ------ Ajouter une activité
            activityServices.createActivity(
                    new Date(),
                    "Add and Share",
                    "Added and shared document ID:" + finalSavedDoc.getId_doc(),
                    globalVariables.getConnectedUser()
            );

            // ------ Log d'information
            LOGGER.info("Added and shared document with ID " + finalSavedDoc.getId_doc()
                    + " by user with ID: "
                    + (globalVariables.getConnectedUser() instanceof User
                    ? globalVariables.getConnectedUser().getId()
                    : ""));

            return ResponseEntity.ok(new ApiResponse(true, "Document created and shared successfully!", finalSavedDoc));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse(false, "Document not created and shared! " + e.getMessage()));
        }
    }

    //
    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> updateNameAndTypeDocument(@RequestBody UpdateDocumentRequest data){

        try{

            System.out.println("updatedDoc: "+ data);
            // Fetch the existing document from the database
            Optional<Document> existingDocumentOptional = docrepository.findById(data.getId_doc());

            if (existingDocumentOptional.isPresent()) {
                Document existingDocument = existingDocumentOptional.get();
                // Update the titre_doc attribute
                existingDocument.setTitre_doc(data.getTitre_doc());

                // TO DO: Update the fichier_doc attribute based on the new titre_doc

                // Update the typeDoc attribute
                existingDocument.setTypeDoc(data.getTypeDoc());
                // Save the updated document
                Document savedDocument = docrepository.save(existingDocument);
                activityServices.createActivity(new Date(), "Update", "Update document ID: " + savedDocument.getId_doc(), globalVariables.getConnectedUser());
                LOGGER.info("Update a document with ID " + savedDocument.getId_doc() + " By User with ID : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
                return ResponseEntity.ok(new ApiResponse(true, "Document updated successfully", savedDocument));

            } else {
                return ResponseEntity.notFound().build();
            }


        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse(false, "Document not updated: " + e.getMessage()));
        }
    }


    // Returning a document by id.
    @GetMapping("/docById/{id}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<DocumentResponse> getDocById(@PathVariable long id) throws Exception {
        Optional<Document> doc = docrepository.findById(id);

        if (doc.isPresent()) {
            activityServices.createActivity(new Date(),"Read","Consulting Document with ID : "+id,globalVariables.getConnectedUser());
            LOGGER.info("Consulting  document ID "+id+" ,By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
            return new ResponseEntity<>(mapper.map(doc.get(), DocumentResponse.class), HttpStatus.OK);
        } else {

            LOGGER.warn("Can't Found Doc ID : "+id+",Consulting By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Returning a list of documents by Rendez-vous id.
    @GetMapping("/docByIdRdv")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public List<DocumentResponse> findDocByIdRdv(@RequestParam Long rdvId) throws Exception {

        activityServices.createActivity(new Date(),"Read","Conslting  documents By Rdv  ID :"+rdvId,globalVariables.getConnectedUser());
        LOGGER.info("Consulting  documents by RDV ID :"+rdvId+" By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return docrepository.getDocByIdRendezvous(rdvId).stream().map(doc -> mapper.map(doc, DocumentResponse.class))
                .collect(Collectors.toList());
    }

    // Returning a list of documents by patient name.
    @GetMapping("/docByNomPatient")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public List<DocumentResponse> findDocByNomPatient(@RequestParam String nomPatient) throws Exception {

        activityServices.createActivity(new Date(),"Read","Conslting  documents Pateint  name :"+nomPatient,globalVariables.getConnectedUser());
        LOGGER.info("Consulting  documents' Patient with name :"+nomPatient+" and User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return docrepository.getDocByNomPatient(nomPatient).stream().map(doc -> mapper.map(doc, DocumentResponse.class))
                .collect(Collectors.toList());
    }

    // Returning a list of documents by patient id.
    @GetMapping("/allDocsByPatientId/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT' , 'ROLE_MEDECIN', 'ROLE_ADMIN')")
    public ResponseEntity<?> findAllDocsByPatientId(@PathVariable("id") long id) {
        try {
            // Log la requête
            String userId = globalVariables.getConnectedUser() != null
                    ? String.valueOf(globalVariables.getConnectedUser().getId())
                    : "Unknown";
            LOGGER.info("Attempting to fetch documents for patient ID: {} by user ID: {}", id, userId);

            // Vérifier si le patient existe
            Patient patient = patientRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + id));

            // Récupérer les documents
            List<Document> documents = docrepository.getDocumentsByPatientId(patient.getId());

            // Enregistrer l'activité
            activityServices.createActivity(
                    new Date(),
                    "Read",
                    String.format("Consulting all documents for patient ID: %d", id),
                    globalVariables.getConnectedUser()
            );

            // Mapper et retourner les résultats
            List<DocumentResponse> response = documents.stream()
                    .map(doc -> mapper.map(doc, DocumentResponse.class))
                    .collect(Collectors.toList());

            LOGGER.info("Successfully retrieved {} documents for patient ID: {}", documents.size(), id);

            return ResponseEntity.ok()
                    .body(response);

        } catch (Exception e) {
            LOGGER.error("Unexpected error while fetching documents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(
                            false,
                            e.getMessage(),
                            null
                    ));
        }
    }

    // Returning all the types of the document.
    @GetMapping("/typeDocuments")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    Iterable<TypeDocumentResponse> getTypeDocuments() throws Exception {
        activityServices.createActivity(new Date(),"Read","Consulting All Type documents",globalVariables.getConnectedUser());
        LOGGER.info("Consulting All Types documents By User ID : {}", globalVariables.getConnectedUser() != null ? globalVariables.getConnectedUser().getId() : "");
        return typeDocumentRepo.findAll().stream().map(typeDoc -> mapper.map(typeDoc, TypeDocumentResponse.class))
                .collect(Collectors.toList());
    }

    // Returning a list of documents by patient id and medecin id
    @GetMapping("/getDocByPatientIdAndMedecin")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public List<DocumentResponse> getDocByPatientIdAndMedecin() throws Exception {

        Optional<Patient> patient = patientRepo.findById(globalVariables.getConnectedUser().getId());

        if (!patient.isPresent()) {
            return Collections.emptyList();
        }else {

            List<Document> documents = docrepository.getDocByPatientIdAndMedecin(patient.get().getId());
            activityServices.createActivity(new Date(), "Read", "Consulting All documents", globalVariables.getConnectedUser());
            LOGGER.info("Consulting All shared documents by patient ID, User ID : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
            return documents.stream().map(doc -> mapper.map(doc, DocumentResponse.class)).collect(Collectors.toList());

        }
    }

    //Get documents of patient with type "PROCHE"
    @GetMapping("/getdocproch")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    Iterable<DocumentResponse> getDocProchPatientId() throws Exception {

        Optional<Patient> patient = patientRepo.findByUserIdAndPatientType(globalVariables.getConnectedUser().getId());

        if (!patient.isPresent()) {
            return Collections.emptyList();
        }else {

            List<Document> documents = docrepository.getDocPatientPROCH(globalVariables.getConnectedUser().getId());

            if(documents.isEmpty()){
                return Collections.emptyList();
            }

            activityServices.createActivity(new Date(),"Read","Consulting All Proch patient  documents by patient",globalVariables.getConnectedUser());
            LOGGER.info("Consulting All PROCHE patient documents by patient with ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

            return documents.stream().map(doc -> mapper.map(doc, DocumentResponse.class)).collect(Collectors.toList());
        }
    }

    //Get documents of patient with type "MOI"
    @GetMapping("/getdocmoi")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public List<DocumentResponse> getDocMoiPatientId() throws Exception {

        Optional<Patient> patient = patientRepo.findById(globalVariables.getConnectedUser().getId());

        if (!patient.isPresent()) {
            return Collections.emptyList();
        }else {

            List<Document> documents = docrepository.getDocPatientMOI(patient.get().getId());

            if(documents.isEmpty()){
                return Collections.emptyList();
            }

            activityServices.createActivity(new Date(),"Read","Consulting All documents for Connected user ",globalVariables.getConnectedUser());
            LOGGER.info("Consulting All documents for connected User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

            return documents.stream().map(doc -> mapper.map(doc, DocumentResponse.class)).collect(Collectors.toList());
        }
    }

    // Archive or unarchive a document specified by its ID
    @PutMapping(path = "/archiveDoc")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> archiveDoc(@RequestParam Long docId, @RequestParam boolean archive)
            throws Exception {

        Optional<Document> document = docrepository.findById(docId);

        if(document.isPresent()){

            //to unarchive document
            if(document.get().getArchived() && !archive){

                document.get().setArchived(archive);
                docrepository.save(document.get());
                activityServices.createActivity(new Date(),"Update","Unarchive document with ID: "+docId,globalVariables.getConnectedUser());
                LOGGER.info("Unarchive document with ID :"+docId+" by User with ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
                return ResponseEntity.ok(new ApiResponse(true, "Unarchive"));

            } else if(!document.get().getArchived() && archive) { //to archive document
                document.get().setArchived(archive);
                docrepository.save(document.get());
                activityServices.createActivity(new Date(), "Update", "Archive document with ID: " + docId, globalVariables.getConnectedUser());
                LOGGER.info("Archive document with ID :" + docId + " by User with ID : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
                return ResponseEntity.ok(new ApiResponse(true, "Archive"));
            }


            LOGGER.info("Archive / Unarchive a document failed by User with ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
            return ResponseEntity.ok(new ApiResponse(false, "No Archive / Unarchive made!"));

        }
        return ResponseEntity.ok(new ApiResponse(false, "No Document found!"));

    }

    @DeleteMapping(path = "/deleteDoc")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @Transactional
    public ResponseEntity<?> deleteDoc(@RequestParam Long docId) {
        try {
            // Vérifier si le document existe
            Optional<Document> documentOptional = docrepository.findById(docId);
            if (documentOptional.isEmpty()) {
                LOGGER.error("Document avec ID : {} introuvable", docId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Document introuvable avec ID : " + docId));
            }

            Document document = documentOptional.get();

            // Supprimer les relations (si applicable)
            // Exemple : Si Document a une relation avec une autre entité comme DocumentMedecin
            if (document.getMedecins() != null && !document.getMedecins().isEmpty()) {
                document.getMedecins().clear();
                docrepository.save(document);
            }

            docrepository.deleteByDocumentId(document.getId_doc());

            // Supprimer le document
            docrepository.delete(document);
            LOGGER.info("Document avec ID : {} supprimé avec succès", docId);

            // Enregistrer une activité pour la suppression
            activityServices.createActivity(new Date(), "Delete", "Suppression du document avec ID : " + docId,
                    globalVariables.getConnectedUser());

            // Retourner une réponse de succès
            return ResponseEntity.ok(new ApiResponse(true, "Document supprimé avec succès."));

        } catch (DataIntegrityViolationException e) {
            // Gérer les contraintes d'intégrité référentielle
            LOGGER.error("Impossible de supprimer le document avec ID : {}, contrainte d'intégrité référentielle.", docId, e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Impossible de supprimer le document en raison de contraintes de données."));
        } catch (Exception e) {
            // Gérer les exceptions inattendues
            LOGGER.error("Erreur lors de la suppression du document avec ID : {}", docId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de la suppression du document. Veuillez réessayer plus tard."));
        }
    }



    // Generating a SAS token for the Azure Blob Storage.
    /*@GetMapping("/getSasToken")
    public ResponseEntity<?> getSAS() throws InvalidKeyException, URISyntaxException, StorageException {

        String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=documentspatient;AccountKey="
                + azureStorageToken + ";EndpointSuffix=core.windows.net";
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        SharedAccessAccountPolicy sharedAccessAccountPolicy = new SharedAccessAccountPolicy();
        sharedAccessAccountPolicy.setPermissionsFromString("racwdlup");
        long date = new Date().getTime();
        long expiryDate = new Date(date + 8640000).getTime();
        sharedAccessAccountPolicy.setSharedAccessStartTime(new Date(date));
        sharedAccessAccountPolicy.setSharedAccessExpiryTime(new Date(expiryDate));
        sharedAccessAccountPolicy.setResourceTypeFromString("sco");
        sharedAccessAccountPolicy.setServiceFromString("bfqt");
        String sasToken = storageAccount.generateSharedAccessSignature(sharedAccessAccountPolicy);

        return ResponseEntity.ok(new ApiResponse(true, sasToken));
    }*/

    // Returning a list of archived documents by patient id.
    @GetMapping("/allArchivedDocByPatientId/{id}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public List<DocumentDTO> findAllArchivedDocsByPatientId(@PathVariable("id") long id) throws Exception {


        patientRepo.findById(id).orElseThrow(()->new Exception("No Match Found"));

        List<Document> documents = docrepository.findByPatientIdAndArchived(id, true);
        activityServices.createActivity(new Date(),"Read","Consulting Archived All  documents by patient ID :"+id,globalVariables.getConnectedUser());
        LOGGER.info("Consulting All Archived  documents by patient ID :"+id+" By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return documents.stream().map(doc -> mapper.map(doc, DocumentDTO.class)).collect(Collectors.toList());
    }

    // Download a file of a patient
    @GetMapping("/download")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> getFile(@RequestParam String fileName) throws Exception {

        //ByteArrayResource resource= new ByteArrayResource(azureAdapter.getFile(fileName));//until we have azure account

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        activityServices.createActivity(new Date(),"Read","Download document ID:",globalVariables.getConnectedUser());
        //LOGGER.info("Download document with Name: "+resource.getFilename()+" By a User with ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

        //return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).headers(headers).body(resource);

        return ResponseEntity.ok(new ApiResponse(true, "Document downloaded successfully!"));
    }


    @PostMapping("/share-documents-to-medecin")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public ResponseEntity<?> shareDocumentsWithMedecin(
            @RequestParam Long medecinId,
            @RequestBody List<Long> documentIds) {

        // Validation des entrées
        if (medecinId == null) {
            return ResponseEntity.badRequest().body("Medecin ID is required");
        }
        if (documentIds == null || documentIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Document IDs list is required and cannot be empty");
        }

        try {
            // Appeler le service pour partager les documents
            Medecin medecin = docservices.shareDocumentsWithMedecin(medecinId, documentIds);

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


    // Endpoint to share a single document with multiple medecins
    @PostMapping("/share-document-to-medecins")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public ResponseEntity<?> shareDocumentWithMedecins(
            @RequestParam Long documentId,
            @RequestBody List<Long> medecinIds) {
        try {
            // Appeler le service pour partager le document avec les médecins
            Map<String, Object> result = docservices.shareDocumentWithMedecins(documentId, medecinIds);

            // Construire le message pour le front-end
            String message;
            if ((int) result.get("newlySharedCount") == 0 && (int) result.get("alreadySharedCount") == medecinIds.size()) {
                message = "Le document est déjà partagé avec tous les médecins sélectionnés.";
            } else if ((int) result.get("newlySharedCount") > 0 && (int) result.get("alreadySharedCount") > 0) {
                message = "Le document a été partagé avec certains médecins, tandis qu'il était déjà partagé avec d'autres.";
            } else if ((int) result.get("newlySharedCount") > 0) {
                message = "Le document a été partagé avec succès avec tous les médecins sélectionnés.";
            } else {
                message = "Aucune opération effectuée.";
            }

            // Retourner une réponse HTTP 200 (OK) avec un message clair et les détails
            return ResponseEntity.ok(new ApiResponse(true, message, result));
        } catch (ResourceNotFoundException e) {
            // Gérer les exceptions spécifiques aux ressources non trouvées
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            // Gérer les erreurs de validation ou d'argument invalide
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            // Gérer toute autre exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erreur lors du partage du document : " + e.getMessage(), null));
        }
    }




}