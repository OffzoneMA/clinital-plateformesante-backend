package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.repository.NotificationRepository;
import com.clinitalPlatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    @Autowired
    private MedecinServiceImpl medecinServiceImpl;
    @Autowired
    private PatientService patientService;


    @Transactional
    public void sendNotificationToUser(Long userId, String title, String message,
                                       String description , String autor, NotificationType type,
                                       boolean requiresAction, String url , LocalDateTime rdvStart , Long rdvId ,Rendezvous rendezvous) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        Notification notification = new Notification();
        Map<String, Object> data = new HashMap<>();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDescription(description);
        notification.setAutor(autor);
        notification.setType(type);
        notification.setRequiresAction(requiresAction);
        notification.setUrl(url);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        if(rdvStart != null){
            notification.setRdvStart(rdvStart);
        }
        logger.info("rendez-vous id" + " " + rdvId);

        if(rdvId != null){
            notification.setRdvId(rdvId);
        }

        if(rendezvous != null){
            //notification.setMedecinId(medecinId);
            data.put("medecinId", rendezvous.getMedecin().getId());
        }

        if(rendezvous != null){
            //notification.setPatientId(patientId);
            data.put("patientId", rendezvous.getPatient().getId());
        }
        notification.setData(data);
        notification = notificationRepository.save(notification);
        NotificationDTO notificationDTO = notificationService.convertToDTO(notification);

        logger.info("📨 Notification envoyée: {}", notificationDTO);

        // Envoyer la notification via WebSocket
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notificationDTO
        );

        logger.info("✅ Notification transmise via WebSocket à /user/{}/queue/notifications", userId);

    }

    @Transactional
    public void sendAppointmentReminder(Long userId, String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId , Rendezvous rendezvous) {
        sendNotificationToUser(
                userId,
                "Rappel de rendez-vous",
                message,
                appointmentDetails,
                autor,
                NotificationType.REMINDER,
                false,
                "/agenda" ,
                rdvStart ,
                rdvId,
                rendezvous
        );
    }

    @Transactional
    public void sendAppointmentCancellation(Long userId , String title , String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId , Rendezvous rendezvous) {

        logger.info("rdv id cancel cancel" + " " + rdvId  );
        sendNotificationToUser(
                userId,
                !title.isEmpty() ? title : "Votre rendez-vous a été annulé",
                message ,
                appointmentDetails,
                autor,
                NotificationType.ERROR,
                false,
                "/agenda",
                rdvStart ,
                rdvId ,
                rendezvous
        );
    }


    @Transactional
    public void sendAppointmentCancellationToMedecin(Long userId , String title , String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId , Rendezvous rendezvous) {

        sendNotificationToUser(
                userId,
                !title.isEmpty() ? title : "Un patient a annulé son rendez-vous",
                message ,
                appointmentDetails,
                autor,
                NotificationType.ERROR,
                false,
                "/agenda",
                rdvStart ,
                rdvId ,
                rendezvous
        );
    }

    public void sendDocumentNotification(Long userId, String title, String message , String details , Long documentId , NotificationType type , String docType , String autor) {

        logger.info("Envoi de la notification pour le document ID: {}", documentId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        boolean exists = notificationRepository.countByUserIdAndTypeAndDataContaining(
                userId,
                type.name(),
                String.valueOf(documentId),
                docType
        ) > 0;

        logger.info("Vérification de l'existence de la notification: {}", exists);

        if (exists) {
            throw new IllegalStateException("Une notification identique a déjà été envoyée.");
        }

        logger.info("Création de la notification pour l'utilisateur ID: {}", userId);

        Notification notification = new Notification();
        Map<String, Object> data = new HashMap<>();
        notification.setUser(user);
        notification.setAutor(autor);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setUrl("/historique-documents");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setDescription(details);

        //notification.setMedecinId(medecinId);
        data.put("documentId", documentId);

        if(docType != null){
            //notification.setPatientId(patientId);
            data.put("docType", docType);
        }

        notification.setData(data);
        notification = notificationRepository.save(notification);
        NotificationDTO notificationDTO = notificationService.convertToDTO(notification);

        logger.info("📨 Notification envoyée: {}", notificationDTO);

        // Envoyer la notification via WebSocket
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notificationDTO
        );

        logger.info("✅ Notification transmise via WebSocket à /user/{}/queue/notifications", userId);
    }

    public void sendShareDocumentsToOneMedecinNotification(Medecin medecinSender , Medecin medecinRecever, List<DocumentMedecin> docs) {
        // Send notifcation to sender
        Notification notification = new Notification();
        notification.setUser(medecinSender.getUser());
        String title = docs.size() > 1 ? "Vos documents ont été partagés avec succès" : "Votre document a été partagé avec succès";
        notification.setTitle(title);
        notification.setMessage(medecinRecever.getNom_med() + " " + medecinRecever.getPrenom_med() + " a reçu votre document" );
        String description = docs.size() > 1 ? "Les fichiers partagés sont désormais accessible au destinataire et consultable depuis son compte." :
                "Le fichier partagé est désormais accessible au destinataire et consultable depuis son compte.";

        notification.setDescription(description);
        notification.setUrl("/share-documents");
        notification.setType(NotificationType.SHARED_DOCUMENT);
        Map<String, Object> data = new HashMap<>();
        data.put("senderId", medecinSender.getId());
        data.put("receiverId", medecinRecever.getId());
        List<Long> documentIds = new ArrayList<>();
        for (DocumentMedecin doc : docs) {
            documentIds.add(doc.getId_doc());
        }
        data.put("documentIds", documentIds);

        notification.setData(data);

        notificationRepository.save(notification);

        // Send notification from socket to sender
        NotificationDTO notificationDTO = notificationService.convertToDTO(notification);
        messagingTemplate.convertAndSendToUser(
                medecinSender.getUser().getId().toString(),
                "/queue/notifications",
                notificationDTO
        );

        // Send notifcation to receiver

        Notification notificationReceiver = new Notification();
        notificationReceiver.setUser(medecinRecever.getUser());
        String titleReceiver = docs.size() > 1 ? "Vous avez reçu de nouveaux documents" : "Vous avez reçu un nouveau document";
        notificationReceiver.setTitle(titleReceiver);
        notificationReceiver.setMessage(medecinSender.getNom_med() + " " + medecinSender.getPrenom_med() + " vous a partagé un document" );
        String descriptionReceiver = docs.size() > 1 ? "Les fichiers partagés sont désormais accessible depuis votre compte." :
                "Le fichier partagé est désormais accessible depuis votre compte.";
        notificationReceiver.setDescription(descriptionReceiver);
        notificationReceiver.setUrl("/share-documents");
        notificationReceiver.setType(NotificationType.SHARED_DOCUMENT);
        Map<String, Object> dataReceiver = new HashMap<>();
        dataReceiver.put("senderId", medecinSender.getId());
        dataReceiver.put("receiverId", medecinRecever.getId());
        List<Long> documentIdsReceiver = new ArrayList<>();
        for (DocumentMedecin doc : docs) {
            documentIdsReceiver.add(doc.getId_doc());
        }
        dataReceiver.put("documentIds", documentIdsReceiver);
        notificationReceiver.setData(dataReceiver);
        notificationRepository.save(notificationReceiver);

        // Send notification from socket to receiver
        NotificationDTO notificationReceiverDTO = notificationService.convertToDTO(notificationReceiver);
        messagingTemplate.convertAndSendToUser(
                medecinRecever.getUser().getId().toString(),
                "/queue/notifications",
                notificationReceiverDTO
        );
    }

    public void sendShareDocumentToMedecins (Medecin medecinSender, List<Medecin> medecinsRecever, List<DocumentMedecin> docs) {
        for (Medecin medecinRecever : medecinsRecever) {
            sendShareDocumentsToOneMedecinNotification(medecinSender, medecinRecever, docs);
        }
    }

    public void sendShareDocumentsToPatientNotification (Medecin medecinSender, Patient patient, List<DocumentMedecin> docs) {
        // Send notification to patient
        Notification notification = new Notification();
        notification.setUser(patient.getUser());
        String title = docs.size() > 1 ? "Vous avez reçu de nouveaux documents" : "Vous avez reçu un nouveau document";
        notification.setTitle(title);
        notification.setMessage(medecinSender.getNom_med() + " " + medecinSender.getPrenom_med() + " a partagé un document avec vous" );
        String description = docs.size() > 1 ? "Les fichiers partagés sont désormais accessible depuis votre compte." :
                "Le fichier partagé est désormais accessible depuis votre compte.";
        notification.setDescription(description);
        notification.setUrl("/share-documents");
        notification.setType(NotificationType.SHARED_DOCUMENT_TO_PATIENT);
        Map<String, Object> data = new HashMap<>();
        data.put("senderId", medecinSender.getId());
        data.put("patientId", patient.getId());
        List<Long> documentIds = new ArrayList<>();
        for (DocumentMedecin doc : docs) {
            documentIds.add(doc.getId_doc());
        }
        data.put("documentIds", documentIds);
        notification.setData(data);

        notificationRepository.save(notification);

        // Send notification from socket to patient
        NotificationDTO notificationDTO = notificationService.convertToDTO(notification);
        messagingTemplate.convertAndSendToUser(
                patient.getUser().getId().toString(),
                "/queue/notifications",
                notificationDTO
        );

        // Send notification to sender
        Notification notificationSender = new Notification();
        notificationSender.setUser(medecinSender.getUser());
        String titleSender = docs.size() > 1 ? "Vos documents ont été partagés avec succès" : "Votre document a été partagé avec succès";
        notificationSender.setTitle(titleSender);
        notificationSender.setMessage(patient.getNom_pat() + " " + patient.getPrenom_pat() + " a reçu votre document" );
        String descriptionSender = docs.size() > 1 ? "Les fichiers partagés sont désormais accessible au patient et consultable depuis son compte." :
                "Le fichier partagé est désormais accessible au patient et consultable depuis son compte.";
        notificationSender.setDescription(descriptionSender);
        notificationSender.setUrl("/share-documents");
        notificationSender.setType(NotificationType.SHARED_DOCUMENT_TO_PATIENT);
        Map<String, Object> dataSender = new HashMap<>();
        dataSender.put("senderId", medecinSender.getId());
        dataSender.put("patientId", patient.getId());
        List<Long> documentIdsSender = new ArrayList<>();
        for (DocumentMedecin doc : docs) {
            documentIdsSender.add(doc.getId_doc());
        }
        dataSender.put("documentIds", documentIdsSender);
        notificationSender.setData(dataSender);
        notificationRepository.save(notificationSender);

        // Send notification from socket to sender
        NotificationDTO notificationSenderDTO = notificationService.convertToDTO(notificationSender);
        messagingTemplate.convertAndSendToUser(
                medecinSender.getUser().getId().toString(),
                "/queue/notifications",
                notificationSenderDTO
        );
    }

    public void sendSharePatientDocumentsNotification (Medecin recever , List<Document> documents , User user) {
        String user_role = user.getRole().name();
        try {
            if(user_role.equals("ROLE_MEDECIN")) {
                Medecin medecin = medecinServiceImpl.getMedecinByUserId(user.getId());

                // Création de la notification pour le médecin receveur
                Notification notification = new Notification();
                notification.setUser(recever.getUser());
                String title = documents.size() > 1 ? "Vous avez reçu de nouveaux documents patient" : "Vous avez reçu un nouveau document patient";
                notification.setTitle(title);
                notification.setMessage(medecin.getNom_med() + " " + medecin.getPrenom_med() + " a partagé des documents avec vous");
                String description = documents.size() > 1 ? "Les fichiers partagés sont désormais accessibles depuis votre compte." :
                        "Le fichier partagé est désormais accessible depuis votre compte.";
                notification.setDescription(description);
                notification.setUrl("/share-documents");
                notification.setType(NotificationType.SHARED_DOCS_PATIENT);
                Map<String, Object> data = new HashMap<>();
                data.put("senderId", medecin.getId());
                data.put("receiverId", recever.getId());
                data.put("senderType", "medecin");
                List<Long> documentIds = new ArrayList<>();
                for (Document doc : documents) {
                    documentIds.add(doc.getId_doc());
                }
                data.put("documentIds", documentIds);
                notification.setData(data);
                notificationRepository.save(notification);
                // Send notification from socket to receiver
                NotificationDTO notificationDTO = notificationService.convertToDTO(notification);
                messagingTemplate.convertAndSendToUser(
                        recever.getUser().getId().toString(),
                        "/queue/notifications",
                        notificationDTO
                );

                // Création de la notification pour le médecin expéditeur
                Notification notificationSender = new Notification();
                notificationSender.setUser(medecin.getUser());
                String titleSender = documents.size() > 1 ? "Vos documents patient ont été partagés avec succès" : "Votre document patient a été partagé avec succès";
                notificationSender.setTitle(titleSender);
                notificationSender.setMessage(recever.getNom_med() + " " + recever.getPrenom_med() + " a reçu vos documents patient");
                String descriptionSender = documents.size() > 1 ? "Les fichiers partagés sont désormais accessibles au destinataire et consultables depuis son compte." :
                        "Le fichier partagé est désormais accessible au destinataire et consultable depuis son compte.";
                notificationSender.setDescription(descriptionSender);
                notificationSender.setUrl("/share-documents");
                notificationSender.setType(NotificationType.SHARED_DOCS_PATIENT);
                Map<String, Object> dataSender = new HashMap<>();
                dataSender.put("senderId", medecin.getId());
                dataSender.put("receiverId", recever.getId());
                dataSender.put("senderType", "medecin");
                List<Long> documentIdsSender = new ArrayList<>();
                for (Document doc : documents) {
                    documentIdsSender.add(doc.getId_doc());
                }
                dataSender.put("documentIds", documentIdsSender);
                notificationSender.setData(dataSender);
                notificationRepository.save(notificationSender);
                // Send notification from socket to sender
                NotificationDTO notificationSenderDTO = notificationService.convertToDTO(notificationSender);
                messagingTemplate.convertAndSendToUser(
                        medecin.getUser().getId().toString(),
                        "/queue/notifications",
                        notificationSenderDTO
                );

            } else if(user_role.equals("ROLE_PATIENT")) {
                // Récupération du patient à partir de l'utilisateur
                Patient patient = patientService.getPatientMoiByUserId(user.getId());

                // Création de la notification pour le médecin receveur
                Notification notification = new Notification();
                notification.setUser(recever.getUser());
                String title = documents.size() > 1 ? "Vous avez reçu de nouveaux documents patient" : "Vous avez reçu un nouveau document patient";
                notification.setTitle(title);
                notification.setMessage(patient.getNom_pat() + " " + patient.getPrenom_pat() + " a partagé des documents avec vous");
                String description = documents.size() > 1 ? "Les fichiers partagés sont désormais accessibles depuis votre compte." :
                        "Le fichier partagé est désormais accessible depuis votre compte.";
                notification.setDescription(description);
                notification.setUrl("/share-documents");
                notification.setType(NotificationType.SHARED_DOCS_PATIENT);
                Map<String, Object> data = new HashMap<>();
                data.put("senderId", patient.getId());
                data.put("receiverId", recever.getId());
                data.put("senderType", "patient");
                List<Long> documentIds = new ArrayList<>();
                for (Document doc : documents) {
                    documentIds.add(doc.getId_doc());
                }
                data.put("documentIds", documentIds);
                notification.setData(data);
                notificationRepository.save(notification);
                // Send notification from socket to receiver
                NotificationDTO notificationDTO = notificationService.convertToDTO(notification);
                messagingTemplate.convertAndSendToUser(
                        recever.getUser().getId().toString(),
                        "/queue/notifications",
                        notificationDTO
                );

                // Création de la notification pour le patient expéditeur
                Notification notificationSender = new Notification();
                notificationSender.setUser(patient.getUser());
                String titleSender = documents.size() > 1 ? "Vos documents patient ont été partagés avec succès" : "Votre document patient a été partagé avec succès";
                notificationSender.setTitle(titleSender);
                notificationSender.setMessage(recever.getNom_med() + " " + recever.getPrenom_med() + " a reçu vos documents patient");
                String descriptionSender = documents.size() > 1 ? "Les fichiers partagés sont désormais accessibles au destinataire et consultables depuis son compte." :
                        "Le fichier partagé est désormais accessible au destinataire et consultable depuis son compte.";
                notificationSender.setDescription(descriptionSender);
                notificationSender.setUrl("/share-documents");
                notificationSender.setType(NotificationType.SHARED_DOCS_PATIENT);
                Map<String, Object> dataSender = new HashMap<>();
                dataSender.put("senderId", patient.getId());
                dataSender.put("receiverId", recever.getId());
                dataSender.put("senderType", "patient");
                List<Long> documentIdsSender = new ArrayList<>();
                for (Document doc : documents) {
                    documentIdsSender.add(doc.getId_doc());
                }
                dataSender.put("documentIds", documentIdsSender);
                notificationSender.setData(dataSender);
                notificationRepository.save(notificationSender);
                // Send notification from socket to sender
                NotificationDTO notificationSenderDTO = notificationService.convertToDTO(notificationSender);
                messagingTemplate.convertAndSendToUser(
                        patient.getUser().getId().toString(),
                        "/queue/notifications",
                        notificationSenderDTO
                );
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de partage de documents au médecin {} : {}", recever.getId(), e.getMessage());
        }
    }
}