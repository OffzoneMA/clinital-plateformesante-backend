package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.Notification;
import com.clinitalPlatform.models.User;
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


    @Transactional
    public void sendNotificationToUser(Long userId, String title, String message,
                                       String description , String autor, NotificationType type,
                                       boolean requiresAction, String url , LocalDateTime rdvStart , Long rdvId , Long medecinId , Long patientId) {
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

        if(medecinId != null){
            //notification.setMedecinId(medecinId);
            data.put("medecinId", medecinId);
        }

        if(patientId != null){
            //notification.setPatientId(patientId);
            data.put("patientId", patientId);
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
    public void sendAppointmentReminder(Long userId, String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId , Long medecinId , Long patientId) {
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
                medecinId,
                patientId
        );
    }

    @Transactional
    public void sendAppointmentCancellation(Long userId, String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId , Long medecinId , Long patientId) {

        logger.info("rdv id cancel cancel" + " " + rdvId  );
        sendNotificationToUser(
                userId,
                "Votre rendez-vous a été annulé",
                message ,
                appointmentDetails,
                autor,
                NotificationType.ERROR,
                false,
                "/agenda",
                rdvStart ,
                rdvId ,
                medecinId,
                patientId
        );
    }


    @Transactional
    public void sendAppointmentCancellationToMedecin(Long userId, String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId , Long medecinId , Long patientId) {

        sendNotificationToUser(
                userId,
                "Un patient a annulé son rendez-vous",
                message ,
                appointmentDetails,
                autor,
                NotificationType.ERROR,
                false,
                "/agenda",
                rdvStart ,
                rdvId ,
                medecinId,
                patientId
        );
    }

    public void sendConsulteDocumentNotification(Long userId, String title, String message , Long documentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        Notification notification = new Notification();
        Map<String, Object> data = new HashMap<>();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.CONSULTE_DOCUMENT);
        notification.setUrl("/historique-documents");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        if(documentId != null){
            //notification.setMedecinId(medecinId);
            data.put("documentId", documentId);
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

}