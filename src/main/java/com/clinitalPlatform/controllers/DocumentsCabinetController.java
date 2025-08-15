package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.DocumentsCabinetDTO;
import com.clinitalPlatform.enums.CabinetDocStateEnum;
import com.clinitalPlatform.models.DocumentsCabinet;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.DocumentsCabinetRequest;
import com.clinitalPlatform.repository.DocumentsCabinetRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.DocumentsCabinetServices;
import com.clinitalPlatform.services.EmailSenderService;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference; // Import correct de Jackson
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/documents-cabinet/")
public class DocumentsCabinetController {

    @Autowired
    private DocumentsCabinetServices documentsCabinetServices;

    @Autowired
    GlobalVariables globalVariables;

    @Autowired
    MedecinRepository medecinRepository;

    @Autowired
    EmailSenderService emailSenderService;

    private static final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private DocumentsCabinetRepository documentsCabinetRepository;

    public DocumentsCabinetDTO toDTO(DocumentsCabinet document) {
        DocumentsCabinetDTO dto = new DocumentsCabinetDTO();
        dto.setId(document.getId());
        dto.setType_doc(document.getType_doc());
        dto.setDate_ajout_doc(document.getDate_ajout_doc());
        dto.setFichier_doc(document.getFichier_doc());
        dto.setNom_fichier(document.getNom_fichier());
        dto.setValidationState(document.getValidationState());

        // Vérification et extraction des IDs
        if (document.getCabinet() != null) {
            dto.setId_cabinet(document.getCabinet().getId_cabinet());
        }
        if (document.getMedecin() != null) {
            dto.setId_medecin(document.getMedecin().getId());
        }

        return dto;
    }

    public List<DocumentsCabinetDTO> toDTOList(List<DocumentsCabinet> documents) {
        return documents.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // **1. Ajouter un document**
    @PreAuthorize("hasAuthority('ROLE_MEDECIN')")
    @PostMapping("/add")
    public ResponseEntity<?> createDocument(@RequestBody DocumentsCabinetRequest request ,
                                            @RequestParam("fichier_doc") MultipartFile fichierDoc) {
        try {

            Long userId = globalVariables.getConnectedUser().getId();

            Optional<Medecin> optionalMedecin = Optional.ofNullable(medecinRepository.getMedecinByUserId(userId));

            // Vérifier si le médecin est trouvé
            if (optionalMedecin.isEmpty()) {
                return ResponseEntity.status(404).body("Aucun médecin associé à cet utilisateur.");
            }

            request.setFichier_doc(fichierDoc.getOriginalFilename());

            request.setNom_fichier(fichierDoc.getOriginalFilename());

            Medecin medecin = optionalMedecin.get();

            // Valider les champs de la requête (par exemple, vérifier que les champs nécessaires ne sont pas vides)
            if (request.getType() == null || request.getFichier_doc() == null) {
                return ResponseEntity.status(400).body("Le type de document et le fichier sont obligatoires.");
            }

            // Création du document via le service
            DocumentsCabinet document = documentsCabinetServices.create(request, medecin);

            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // **2. Ajouter plusieurs documents**
    @PreAuthorize("hasAuthority('ROLE_MEDECIN')")
    @PostMapping("/add-multiple")
    public ResponseEntity<?> createMultipleDocuments(
            @RequestParam("fichier_docs") List<MultipartFile> fichierDocs,  // Liste de fichiers
            @RequestParam("documentRequestIds") String documentRequestIds, // Identifiants uniques
            @RequestParam("documentsMetadata") String documentsMetadataJson) {  // Métadonnées des documents

        try {
            // Désérialiser les métadonnées des documents
            ObjectMapper objectMapper = new ObjectMapper();
            List<DocumentsCabinetRequest> requests = objectMapper.readValue(documentsMetadataJson, new TypeReference<List<DocumentsCabinetRequest>>() {});

            List<String> parsedDocumentRequestIds = objectMapper.readValue(documentRequestIds, new TypeReference<List<String>>() {});


            // Vérifier que le nombre de fichiers et de requêtes est le même
            if (fichierDocs.size() != requests.size()) {
                return ResponseEntity.status(400).body("Le nombre de fichiers ne correspond pas au nombre de documents.");
            }

            // Récupérer l'ID de l'utilisateur connecté
            Long userId = globalVariables.getConnectedUser().getId();

            Optional<Medecin> optionalMedecin = Optional.ofNullable(medecinRepository.getMedecinByUserId(userId));

            if (optionalMedecin.isEmpty()) {
                return ResponseEntity.status(404).body("Aucun médecin associé à cet utilisateur.");
            }

            Medecin medecin = optionalMedecin.get();

            List<DocumentsCabinet> documentsCabinets = new ArrayList<>();

            // Pour chaque fichier, vérifier la correspondance avec son identifiant
            for (int i = 0; i < fichierDocs.size(); i++) {
                MultipartFile fichierDoc = fichierDocs.get(i);
                DocumentsCabinetRequest request = requests.get(i);
                String documentRequestId = parsedDocumentRequestIds.get(i);  // Identifiant du document

                // Vérifier que le documentRequestId correspond à la requête
                if (!documentRequestId.equals(request.getType().name() + "_" + i)) {
                    return ResponseEntity.status(400)
                            .body(String.format("Le fichier ne correspond pas aux métadonnées. Attendu: %s, Reçu: %s", request.getType().name() + "_" + i, documentRequestId));
                }

                // Upload du fichier et récupération de son URL (Azure par exemple)
                //String fileUrl = azureBlobStorageService.uploadFile(fichierDoc);

                // Mettre à jour le champ fichier_doc avec l'URL du fichier téléchargé
                request.setFichier_doc(fichierDoc.getOriginalFilename());

                request.setNom_fichier(fichierDoc.getOriginalFilename());

                // Créer le document et l'ajouter à la base de données
                DocumentsCabinet document = documentsCabinetServices.create(request, medecin);

                documentsCabinets.add(document);
            }

            List<DocumentsCabinetDTO> documentDTOs = this.toDTOList(documentsCabinets);

            return ResponseEntity.ok(documentDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la création des documents : " + e.getMessage());
        }
    }

    // **2. Récupérer les documents d'un médecin**
    @GetMapping("/medecin")
    @PreAuthorize("hasAuthority('ROLE_MEDECIN')")
    public ResponseEntity<?> getDocumentsByMedecin() {
        try {
            // Vérifier si un utilisateur est connecté
            if (globalVariables.getConnectedUser() == null) {
                return ResponseEntity.status(401).body("Utilisateur non connecté.");
            }

            Long userId = globalVariables.getConnectedUser().getId();
            Optional<Medecin> optionalMedecin = Optional.ofNullable(medecinRepository.getMedecinByUserId(userId));

            // Vérifier si le médecin est trouvé
            if (optionalMedecin.isEmpty()) {
                return ResponseEntity.status(404).body("Aucun médecin associé à cet utilisateur.");
            }

            Medecin medecin = optionalMedecin.get();
            List<DocumentsCabinet> documents = documentsCabinetServices.getDocumentsByMedecin(medecin.getId());

            // Vérifier si le médecin a des documents
            if (documents.isEmpty()) {
                return ResponseEntity.status(404).body("Aucun document trouvé pour ce médecin.");
            }

            List<DocumentsCabinetDTO> documentDTOs = this.toDTOList(documents);

            return ResponseEntity.ok(documentDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur interne du serveur : " + e.getMessage());
        }
    }

    // 🔹 Endpoint pour récupérer les documents d'un médecin par son ID
    @GetMapping("/by-medecin/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN' , 'ROLE_MEDECIN')")
    public ResponseEntity<?> getDocumentsByMedecinId(@PathVariable Long id) {
        try {
            Optional<Medecin> optionalMedecin = medecinRepository.findById(id);

            // Vérifier si le médecin existe
            if (optionalMedecin.isEmpty()) {
                return ResponseEntity.status(404).body("Médecin non trouvé.");
            }

            Medecin medecin = optionalMedecin.get();
            List<DocumentsCabinet> documents = documentsCabinetServices.getDocumentsByMedecin(medecin.getId());

            // Vérifier si des documents existent
            if (documents.isEmpty()) {
                return ResponseEntity.status(404).body("Aucun document trouvé pour ce médecin.");
            }

            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur interne du serveur : " + e.getMessage());
        }
    }

    // **3. Récupérer les documents d'un cabinet**
    @GetMapping("/cabinet/{cabinetId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN' , 'ROLE_MEDECIN')")
    public ResponseEntity<List<DocumentsCabinet>> getDocumentsByCabinet(@PathVariable Long cabinetId) {
        return ResponseEntity.ok(documentsCabinetServices.getDocumentsByCabinet(cabinetId));
    }

    // **5. Valider un document**
    @PutMapping("/valider/{documentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DocumentsCabinet> validerDocument(@PathVariable Long documentId) {
        try {
            return ResponseEntity.ok(documentsCabinetServices.validerDocument(documentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update-docs-status/bymedecin/{medecinId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateDocumentsStatusByMedecin(@PathVariable Long medecinId, @RequestBody CabinetDocStateEnum status) {
        try {
            Optional<Medecin> optionalMedecin = medecinRepository.findById(medecinId);

            // Vérifier si le médecin existe
            if (optionalMedecin.isEmpty()) {
                return ResponseEntity.status(404).body("Médecin non trouvé.");
            }

            Medecin medecin = optionalMedecin.get();

            List<DocumentsCabinet> documents = documentsCabinetServices.getDocumentsByMedecin(medecin.getId());
            if (documents.isEmpty()) {
                return ResponseEntity.status(404).body("Aucun document trouvé pour ce médecin.");
            }
            // Mettre à jour le statut de chaque document
            for (DocumentsCabinet document : documents) {
                document.setValidationState(status);
                documentsCabinetRepository.save(document);
            }

            // Envoyer un email de notification (si nécessaire)
            if( status == CabinetDocStateEnum.VALID || status == CabinetDocStateEnum.REJECTED || status == CabinetDocStateEnum.EN_TRAITEMENT) {
                emailSenderService.sendUpdateMedecinCabinetDocsStatus(medecin.getUser().getEmail() ,status);
            }
            return ResponseEntity.ok("Statuts des documents mis à jour avec succès pour le médecin ID: " + medecinId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la mise à jour des statuts : " + e.getMessage());
        }
    }

    // **6. Rejeter un document**
    @PutMapping("/rejeter/{documentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DocumentsCabinet> rejeterDocument(@PathVariable Long documentId) {
        try {
            return ResponseEntity.ok(documentsCabinetServices.rejeterDocument(documentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 🔹 Modifier un document
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEDECIN')")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestBody DocumentsCabinetRequest request) {
        try {
            DocumentsCabinet updatedDoc = documentsCabinetServices.updateDocument(id, request);
            return ResponseEntity.ok(updatedDoc);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Erreur : " + e.getMessage());
        }
    }

    // 🔹 Supprimer un document
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentsCabinetServices.deleteDocument(id);
            return ResponseEntity.ok("Document supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Erreur : " + e.getMessage());
        }
    }

}
