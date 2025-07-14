package com.clinitalPlatform.services;

import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.DocumentRequest;
import com.clinitalPlatform.repository.DocumentMedecinRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.repository.TypeDocumentRepository;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentMedecinService {

    @Autowired
    private DocumentMedecinRepository repository;

    @Autowired
    private TypeDocumentRepository typeDocumentRepo;

    @Autowired
    private MedecinRepository medecinRepo;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private MedecinService medecinService;

    public DocumentMedecin save(DocumentMedecin doc) {
        return repository.save(doc);
    }

    public List<DocumentMedecin> getByMedecin(Long medecinId) {
        return repository.findByMedecinAuteurId(medecinId);
    }

    public Optional<DocumentMedecin> getById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public DocumentMedecin create(String document) throws Exception{
        try {

            ObjectMapper om = new ObjectMapper();

            DocumentRequest documentReq = om.readValue(document, DocumentRequest.class);

            Medecin medecin = null;

            TypeDocument typedoc= typeDocumentRepo.findById(documentReq.getTypeDocId()).orElseThrow(()->new Exception("No Matching type doc found"));
            DocumentMedecin documentEntity = new DocumentMedecin();
            documentEntity.setTitre_doc(documentReq.getTitre_doc());
            documentEntity.setTypeDoc(typedoc);
            documentEntity.setArchived(false);
            documentEntity.setDate_ajout_doc(new Date());
            documentEntity.setCategorie(documentReq.getCategorie());
            if (documentReq.getMedecinId() != null && documentReq.getMedecinId() != 0) {
                Optional<Medecin> optionalMedecin = medecinRepo.findById(documentReq.getMedecinId());
                if (optionalMedecin.isPresent()) {
                    medecin = optionalMedecin.get();
                    documentEntity.setMedecinAuteur(medecin);
                }
            }
            repository.save(documentEntity);
            return documentEntity;
        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }

    public Medecin shareDocumentMedWithMedecin(Long medecinId, List<Long> documentIds , Long userId) throws Exception {
        if(medecinId == null || documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Medecin ID and Document IDs must not be null or empty");
        }

        Medecin sender  = medecinService.getMedecinByUserId(userId);

        Medecin medecin = medecinRepo.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Medecin not found with ID: " + medecinId));

        List<DocumentMedecin> documents = repository.findAllById(documentIds);

        if(documents.isEmpty()) {
            throw new IllegalArgumentException("No documents found with the provided IDs");
        }

        List<Long> missingDocuments = documentIds.stream()
                .filter(id -> documents.stream().noneMatch(doc -> doc.getId_doc().equals(id)))
                .toList();
        if(!missingDocuments.isEmpty()) {
            throw new IllegalArgumentException("Some documents not found with IDs: " + missingDocuments);
        }

        // Filtré les documents qui ne sont pas déjà partagés avec le médecin
        List<DocumentMedecin> sharedDocuments = documents.stream()
                .filter(doc -> !doc.getMedecinsPartages().contains(medecin))
                .toList();

        if(sharedDocuments.isEmpty()) {
            throw new IllegalStateException("No documents to share with the Medecin ");
        }

        // Ajoute le médecin aux documents partagés
        sharedDocuments.forEach(doc -> {
            doc.getMedecinsPartages().add(medecin);
            repository.save(doc);
        });

        // Envoie une notification au médecin

        pushNotificationService.sendShareDocumentsToOneMedecinNotification(sender , medecin , sharedDocuments);
        return medecin;
    }

    public List<DocumentMedecin> getSharedDocumentsByMedecin(Long medecinId) {
        if(medecinId == null) {
            throw new IllegalArgumentException("Medecin ID must not be null");
        }
        return repository.findByMedecinsPartagesId(medecinId);
    }

    public Patient shareDocumentMedWithPatient(Long patientId, List<Long> documentIds , Long userId) throws Exception {
        if(patientId == null || documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Patient ID and Document IDs must not be null or empty");
        }

        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));

        Medecin sender  = medecinService.getMedecinByUserId(userId);

        List<DocumentMedecin> documents = repository.findAllById(documentIds);

        if(documents.isEmpty()) {
            throw new IllegalArgumentException("No documents found with the provided IDs");
        }

        List<Long> missingDocuments = documentIds.stream()
                .filter(id -> documents.stream().noneMatch(doc -> doc.getId_doc().equals(id)))
                .toList();

        if(!missingDocuments.isEmpty()) {
            throw new IllegalArgumentException("Some documents not found with IDs: " + missingDocuments);
        }

        // Filtré les documents qui ne sont pas déjà partagés avec le patient
        List<DocumentMedecin> sharedDocuments = documents.stream()
                .filter(doc -> !doc.getPatientsPartages().contains(patient))
                .toList();

        if(sharedDocuments.isEmpty()) {
            throw new IllegalStateException("No documents to share with the Patient");
        }

        // Ajoute le patient aux documents partagés
        sharedDocuments.forEach(doc -> {
            doc.getPatientsPartages().add(patient);
            repository.save(doc);
        });

        // Envoie une notification au patient
        pushNotificationService.sendShareDocumentsToPatientNotification(sender, patient, sharedDocuments);
        return patient;
    }

    public List<DocumentMedecin> getSharedDocumentsWithMedecin(Long medecinId) {
        if(medecinId == null) {
            throw new IllegalArgumentException("Medecin ID must not be null");
        }
        return repository.findByMedecinsPartagesId(medecinId);
    }

    public List<DocumentMedecin> getSharedDocumentsWithPatient(Long patientId) {
        if(patientId == null) {
            throw new IllegalArgumentException("Patient ID must not be null");
        }
        return repository.findByPatientsPartagesId(patientId);
    }
}

