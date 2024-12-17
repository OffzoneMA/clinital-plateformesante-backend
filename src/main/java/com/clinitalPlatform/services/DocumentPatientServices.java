package com.clinitalPlatform.services;

import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.DocumentRequest;
import com.clinitalPlatform.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        Optional<Medecin> optionalMedecin = medecinRepo.findById(medecinId);

        if (!optionalMedecin.isPresent()) {
            throw new IllegalArgumentException("Medecin with ID " + medecinId + " not found");
        }

        Medecin medecin = optionalMedecin.get();
        List<Document> documents = docrepo.findAllById(documentIds);

        medecin.getMeddoc().addAll(documents);
        return medecinRepo.save(medecin);
    }*/

    public Medecin shareDocumentsWithMedecin(Long medecinId, List<Long> documentIds) {
        // Vérifier si le médecin existe
        Medecin medecin = medecinRepo.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Medecin with ID " + medecinId + " not found"));

        // Récupérer les documents associés aux IDs donnés
        List<Document> documents = docrepo.findAllById(documentIds);


        // Filtrer les documents qui ne sont pas déjà associés au médecin
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

    public Document shareDocumentWithMedecins(Long documentId, List<Long> medecinIds) {
        // Vérifier si le document existe
        Document document = docrepo.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document with ID " + documentId + " not found"));

        // Récupérer la liste des médecins par leurs IDs
        List<Medecin> medecins = medecinRepo.findAllById(medecinIds);

        if (medecins.isEmpty()) {
            throw new IllegalArgumentException("No valid Medecins found for the provided IDs");
        }

        // Ajouter le document uniquement aux médecins qui ne l'ont pas déjà
        medecins.forEach(medecin -> {
            if (medecin.getMeddoc() == null) {
                medecin.setMeddoc(new ArrayList<>());
            }
            if (!medecin.getMeddoc().contains(document)) {
                medecin.getMeddoc().add(document);
            }
        });

        // Sauvegarder les médecins mis à jour
        medecinRepo.saveAll(medecins);

        // Retourner le document partagé
        return document;
    }

}
