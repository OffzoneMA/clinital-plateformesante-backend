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
import java.util.List;

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
                                       boolean requiresAction, String url , LocalDateTime rdvStart , Long rdvId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        Notification notification = new Notification();
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
    public void sendAppointmentReminder(Long userId, String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId) {
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
                rdvId
        );
    }

    @Transactional
    public void sendAppointmentCancellation(Long userId, String message , String appointmentDetails , String autor , LocalDateTime rdvStart , Long rdvId) {

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
                rdvId
        );
    }

}