package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.DocumentDTO;
import com.clinitalPlatform.models.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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

        Optional<Patient> patient = patientRepo.findById(globalVariables.getConnectedUser().getId());

        // Verify if the list of patient is empty
        if (!patient.isPresent()) {
            return Collections.emptyList();
        }else {

            activityServices.createActivity(new Date(), "Read", "Consulting all Documents", globalVariables.getConnectedUser());
            LOGGER.info("Conslting All documents By User ID : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
            return docrepository.findByPatientId(patient.get().getId())
                    .stream().map(document -> mapper.map(document, DocumentResponse.class)).collect(Collectors.toList());
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

            savedDoc.setNumero_doc(savedDoc.getId_doc());

            // --------------- update saved doc
            Document finalSavedDoc = docrepository.save(savedDoc);
            // ----- add doc info to RDV :
            Rendezvous rdv= rdvRepository.findById(finalSavedDoc.getRendezvous().getId()).orElseThrow(()->new Exception("NO MATCHING FOUND"));
            rdv.getDocuments().add(finalSavedDoc);

            //update rendez-vous
            rdvRepository.save(rdv);

            activityServices.createActivity(new Date(),"Add","Add New document ID:"+finalSavedDoc.getId_doc(),globalVariables.getConnectedUser());
            LOGGER.info("Add new  documents ID :"+finalSavedDoc.getId_doc()+" By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
            return ResponseEntity.ok(new ApiResponse(true, "Document created successfully!",finalSavedDoc));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse(false, "Document not created!"+e.getMessage()));

        }
    }

    // Returning a document by id.
    @GetMapping("/docById/{id}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<DocumentResponse> getDocById(@PathVariable long id) throws Exception {
        Optional<Document> tutorialData = docrepository.findById(id);

        if (tutorialData.isPresent()) {
            activityServices.createActivity(new Date(),"Read","Consulting Document with ID : "+id,globalVariables.getConnectedUser());
            LOGGER.info("Conslting  document ID "+id+" ,By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
            return new ResponseEntity<>(mapper.map(tutorialData.get(), DocumentResponse.class), HttpStatus.OK);
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
        LOGGER.info("Conslting  documents by RDV ID :"+rdvId+" By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
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
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public List<DocumentResponse> findAllDocsByPatientId(@PathVariable("id") long id ) throws Exception {


        Patient patient=patientRepo.findById(id).orElseThrow(()->new Exception("No Such Patient Found"));

        List<Document> documents = docrepository.findByPatientId(patient.getId());
        activityServices.createActivity(new Date(),"Read","Consulting All  documents by patient ID :"+id,globalVariables.getConnectedUser());
        LOGGER.info("Consulting All  documents by patient ID :"+id+" By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return documents.stream().map(doc -> mapper.map(doc, DocumentResponse.class)).collect(Collectors.toList());
    }

    // Returning all the types of the document.
    @GetMapping("/typeDocuments")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    Iterable<TypeDocumentResponse> getTypeDocuments() throws Exception {
        activityServices.createActivity(new Date(),"Read","Consulting All Type  documents",globalVariables.getConnectedUser());
        LOGGER.info("Consulting All Types  documents By User ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return typeDocumentRepo.findAll().stream().map(typeDoc -> mapper.map(typeDoc, TypeDocumentResponse.class))
                .collect(Collectors.toList());
    }

    // Returning a list of documents by patient id and medecin id
    @GetMapping("/getDocByPatientIdAndMedecin")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public List<DocumentResponse> getDocByPatientIdAndMedecin() throws Exception {

        Optional<Patient> patient = patientRepo.findById(globalVariables.getConnectedUser().getId());

        if (!patient.isPresent()) {
            return Collections.emptyList();
        }else {

            List<Document> documents = docrepository.getDocByPatientIdAndMedecin(patient.get().getId());
            activityServices.createActivity(new Date(), "Read", "Consulting All documents", globalVariables.getConnectedUser());
            LOGGER.info("Consulting All Archived  documents by patient ID, User ID : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
            return documents.stream().map(doc -> mapper.map(doc, DocumentResponse.class)).collect(Collectors.toList());

        }
    }

    //Get documents of patient with type "PROCH"
    @GetMapping("/getdocproch")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @ResponseBody
    public List<DocumentResponse> getDocProchPatientId() throws Exception {


        Optional<Patient> patient = patientRepo.findById(globalVariables.getConnectedUser().getId());

        if (!patient.isPresent()) {
            return Collections.emptyList();
        }else {

            List<Document> documents = docrepository.getDocPatientPROCH(patient.get().getId());

            if(documents.isEmpty()){
                return Collections.emptyList();
            }

            activityServices.createActivity(new Date(),"Read","Consulting All Proch patient  documents by patient",globalVariables.getConnectedUser());
            LOGGER.info("Consulting All PROCH patient documents by patient with ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

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
    @PostMapping(path = "/archiveDoc")
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
    public ResponseEntity<?> deleteDoc(@RequestParam Long docId) throws Exception {

        Optional<Document> document = docrepository.findById(docId);

        docrepository.delete(document.get());
        //azureAdapter.deleteBlob(document.get().getFichier_doc());
        activityServices.createActivity(new Date(),"Delete","Delete document with ID :"+docId,globalVariables.getConnectedUser());
        LOGGER.info("Delete document with ID :"+docId+" by User with ID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return ResponseEntity.ok(new ApiResponse(true, "Document has been Deleted"));

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

}
