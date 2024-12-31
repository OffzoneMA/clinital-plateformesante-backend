package com.clinitalPlatform.services;

import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.DocumentRequest;
import com.clinitalPlatform.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class DocumentPatientServices {

    @Autowired
    private DocumentRepository docrepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private RdvRepository rdvRepository;

    @Autowired
    private TypeDocumentRepository typeDocumentRepo;

    @Autowired
    private MedecinRepository medecinRepo;

    public Document create(String document) throws Exception{
        try {

            ObjectMapper om = new ObjectMapper();
			
            DocumentRequest documentReq = om.readValue(document, DocumentRequest.class);
            Patient patient = patientRepo.findById(documentReq.getPatientId()).orElseThrow(()->new Exception("No Matching Patient found"));
            //Rendezvous rendezvous =  rdvRepository.findById(documentReq.getRdvId()).orElseThrow(()->new Exception("No Matching RDV found"));
            Rendezvous rendezvous =  null;
            TypeDocument typedoc= typeDocumentRepo.findById(documentReq.getTypeDocId()).orElseThrow(()->new Exception("No Matching type doc found"));
            Document documentEntity = new Document();
            documentEntity.setTitre_doc(documentReq.getTitre_doc());
            documentEntity.setTypeDoc(typedoc);
            documentEntity.setPatient(patient);
            documentEntity.setDossier(patient.getDossierMedical());
            documentEntity.setArchived(false);
            documentEntity.setDate_ajout_doc(new Date());
            documentEntity.setRendezvous(rendezvous);
            documentEntity.setAuteur(documentReq.getAuteur());
            docrepo.save(documentEntity);
            return documentEntity;
        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e.getMessage());
        }
    }

    /*public Medecin shareDocumentsWithMedecin(Long medecinId, List<Long> documentIds) {
        // Vérifier si le médecin existe
        Medecin medecin = medecinRepo.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Medecin with ID " + medecinId + " not found"));

        // Récupérer les documents associés aux IDs donnés
        List<Document> documents = docrepo.findAllById(documentIds);

        // Filtrer les documents déjà partagés
        List<Document> newDocuments = documents.stream()
                .filter(doc -> !medecin.getMeddoc().contains(doc))
                .collect(Collectors.toList());


        if (newDocuments.isEmpty()) {
            // Aucun nouveau document à partager
            throw new IllegalStateException("All documents are already shared with Medecin ID: " + medecinId);
        }

        // Ajouter uniquement les nouveaux documents au médecin
        medecin.getMeddoc().addAll(newDocuments);

        // Sauvegarder les modifications dans la base de données
        return medecinRepo.save(medecin);
    }*/

    @Transactional
    public Medecin shareDocumentsWithMedecin(Long medecinId, List<Long> documentIds) {
        // Vérification des paramètres
        if (medecinId == null) {
            throw new IllegalArgumentException("Medecin ID cannot be null");
        }
        if (documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Document IDs cannot be null or empty");
        }

        // Récupération du médecin
        Medecin medecin = medecinRepo.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Medecin with ID " + medecinId + " not found"));

        // Récupération des documents
        List<Document> documents = docrepo.findAllById(documentIds);

        if (documents.isEmpty()) {
            throw new IllegalArgumentException("No documents found for the provided IDs: " + documentIds);
        }

        // Vérifier s'il manque des documents par rapport aux IDs fournis
        List<Long> missingDocumentIds = documentIds.stream()
                .filter(id -> documents.stream().noneMatch(doc -> doc.getId_doc().equals(id)))
                .collect(Collectors.toList());
        if (!missingDocumentIds.isEmpty()) {
            throw new IllegalArgumentException("Documents not found for IDs: " + missingDocumentIds);
        }

        // Filtrer les documents non encore associés au médecin
        List<Document> newDocuments = documents.stream()
                .filter(doc -> !medecin.getMeddoc().contains(doc))
                .collect(Collectors.toList());

        if (newDocuments.isEmpty()) {
            throw new IllegalStateException("All documents are already shared with Medecin ID: " + medecinId);
        }

        // Ajouter les nouveaux documents au médecin
        medecin.getMeddoc().addAll(newDocuments);

        // Sauvegarder les modifications
        Medecin updatedMedecin = medecinRepo.save(medecin);

        // Log pour le suivi

        return updatedMedecin;
    }


    // Share a single document with multiple medecins
    /*public Document shareDocumentWithMedecins(Long documentId, List<Long> medecinIds) {
        Optional<Document> optionalDocument = docrepo.findById(documentId);

        if (!optionalDocument.isPresent()) { // Remplace isEmpty() par !isPresent()
            throw new IllegalArgumentException("Document with ID " + documentId + " not found");
        }

        Document document = optionalDocument.get();
        List<Medecin> medecins = medecinRepo.findAllById(medecinIds);

        medecins.forEach(medecin -> medecin.getMeddoc().add(document));
        medecinRepo.saveAll(medecins);

        return document;
    }*/

    public Map<String, Object> shareDocumentWithMedecins(Long documentId, List<Long> medecinIds) {
        // Validation des entrées
        if (documentId == null || medecinIds == null || medecinIds.isEmpty()) {
            throw new IllegalArgumentException("Document ID and Medecin IDs cannot be null or empty");
        }

        // Vérifier si le document existe
        Document document = docrepo.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document with ID " + documentId + " not found"));

        // Récupérer la liste des médecins par leurs IDs
        List<Medecin> medecins = medecinRepo.findAllById(medecinIds);

        // Vérifier si tous les IDs de médecins fournis sont valides
        Set<Long> foundIds = medecins.stream()
                .map(Medecin::getId)
                .collect(Collectors.toSet());
        List<Long> invalidIds = medecinIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        if (!invalidIds.isEmpty()) {
            throw new ResourceNotFoundException("Medecins", "Id", invalidIds);
        }

        // Initialiser les compteurs et les listes pour le rapport
        int alreadySharedCount = 0;
        int newlySharedCount = 0;
        List<Medecin> updatedMedecins = new ArrayList<>();
        List<Long> newlySharedMedecinIds = new ArrayList<>();
        List<Long> alreadySharedMedecinIds = new ArrayList<>();

        // Traiter chaque médecin
        for (Medecin medecin : medecins) {
            // Initialiser la liste des documents si elle est null
            if (medecin.getMeddoc() == null) {
                medecin.setMeddoc(new ArrayList<>());
            }

            // Vérifier si le médecin a déjà le document
            if (medecin.getMeddoc().contains(document)) {
                alreadySharedCount++;
                alreadySharedMedecinIds.add(medecin.getId());
                continue;
            }

            // Ajouter le document au médecin
            medecin.getMeddoc().add(document);
            updatedMedecins.add(medecin);
            newlySharedMedecinIds.add(medecin.getId());
            newlySharedCount++;
        }

        // Sauvegarder uniquement les médecins qui ont été modifiés
        if (!updatedMedecins.isEmpty()) {
            medecinRepo.saveAll(updatedMedecins);
        }

        // Préparer un message clair pour le résultat
        Map<String, Object> result = new HashMap<>();
        if (newlySharedCount == 0 && alreadySharedCount == medecinIds.size()) {
            result.put("message", "Le document est déjà partagé avec tous les médecins sélectionnés.");
        } else if (newlySharedCount > 0 && alreadySharedCount > 0) {
            result.put("message", "Le document a été partagé avec certains médecins, tandis qu'il était déjà partagé avec d'autres.");
        } else if (newlySharedCount > 0) {
            result.put("message", "Le document a été partagé avec succès avec tous les médecins sélectionnés.");
        }

        result.put("documentId", documentId);
        result.put("newlySharedCount", newlySharedCount);
        result.put("alreadySharedCount", alreadySharedCount);
        result.put("totalMedecinsProcessed", medecinIds.size());
        result.put("newlySharedMedecinIds", newlySharedMedecinIds);
        result.put("alreadySharedMedecinIds", alreadySharedMedecinIds);
        result.put("invalidMedecinIds", invalidIds);

        return result;
    }

}
