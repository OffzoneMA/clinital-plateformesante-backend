package com.clinitalPlatform.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.clinitalPlatform.enums.CabinetDocStateEnum;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.DocumentsCabinet;
import com.clinitalPlatform.payload.request.DocumentsCabinetRequest;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.DocumentsCabinetRepository;
@Transactional
@Service
public class DocumentsCabinetServices {

    @Autowired
    private CabinetRepository cabrepo;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private DocumentsCabinetRepository docrepo;

    // **1. Ajouter un document**
    public DocumentsCabinet create(DocumentsCabinetRequest docreq , Medecin medecin) throws Exception {
        Cabinet cabinet = cabrepo.findById(medecin.getFirstCabinetId())
                .orElseThrow(() -> new Exception("No Matching Cabinet found"));

        DocumentsCabinet documentEntity = new DocumentsCabinet();
        documentEntity.setType_doc(docreq.getType());
        documentEntity.setDate_ajout_doc(LocalDate.now());
        documentEntity.setFichier_doc(docreq.getFichier_doc());
        documentEntity.setNom_fichier(docreq.getNom_fichier());
        documentEntity.setCabinet(cabinet);
        documentEntity.setMedecin(medecin);
        documentEntity.setValidationState(CabinetDocStateEnum.EN_COURS);

        return docrepo.save(documentEntity);
    }

    // **2. Ajouter plusieurs documents**
    public List<DocumentsCabinet> createMultipleDocuments(List<DocumentsCabinetRequest> docRequests , Medecin medecin) throws Exception {
        List<DocumentsCabinet> documents = new ArrayList<>();

        for (DocumentsCabinetRequest docreq : docRequests) {
            Cabinet cabinet = cabrepo.findById(medecin.getFirstCabinetId())
                    .orElseThrow(() -> new Exception("No Matching Cabinet found"));

            DocumentsCabinet documentEntity = new DocumentsCabinet();
            documentEntity.setType_doc(docreq.getType());
            documentEntity.setDate_ajout_doc(LocalDate.now());
            documentEntity.setFichier_doc(docreq.getFichier_doc());
            documentEntity.setNom_fichier(documentEntity.getNom_fichier());
            documentEntity.setCabinet(cabinet);
            documentEntity.setMedecin(medecin);
            documentEntity.setValidationState(CabinetDocStateEnum.EN_COURS);

            documents.add(documentEntity);
        }

        return docrepo.saveAll(documents);
    }

    // **2. Obtenir tous les documents d'un médecin**
    public List<DocumentsCabinet> getDocumentsByMedecin(Long medecinId) {
        return docrepo.findByMedecinId(medecinId);
    }

    // **3. Obtenir tous les documents d'un cabinet**
    public List<DocumentsCabinet> getDocumentsByCabinet(Long cabinetId) {
        return docrepo.findByCabinetId(cabinetId);
    }

    // 🔹 Modifier un document
    public DocumentsCabinet updateDocument(Long id, DocumentsCabinetRequest docreq) throws Exception {
        DocumentsCabinet document = docrepo.findById(id)
                .orElseThrow(() -> new Exception("Document non trouvé."));

        // Mise à jour des champs modifiables
        if (docreq.getType() != null) document.setType_doc(docreq.getType());
        if (docreq.getFichier_doc() != null) document.setFichier_doc(docreq.getFichier_doc());
        if (docreq.getDocstate() != null) document.setValidationState(docreq.getDocstate());
        if(docreq.getNom_fichier() != null) document.setNom_fichier(document.getNom_fichier());

        return docrepo.save(document);
    }

    // 🔹 Supprimer un document
    public void deleteDocument(Long id) throws Exception {
        Optional<DocumentsCabinet> document = docrepo.findById(id);

        if (document.isEmpty()) {
            throw new Exception("Document non trouvé.");
        }

        docrepo.deleteById(id);
    }

    // **5. Valider un document**
    public DocumentsCabinet validerDocument(Long documentId) throws Exception {
        DocumentsCabinet document = docrepo.findById(documentId)
                .orElseThrow(() -> new Exception("Document not found"));
        document.setValidationState(CabinetDocStateEnum.VALID);
        return docrepo.save(document);
    }

    // **6. Rejeter un document**
    public DocumentsCabinet rejeterDocument(Long documentId) throws Exception {
        DocumentsCabinet document = docrepo.findById(documentId)
                .orElseThrow(() -> new Exception("Document not found"));
        document.setValidationState(CabinetDocStateEnum.REJECTED);
        return docrepo.save(document);
    }

}
