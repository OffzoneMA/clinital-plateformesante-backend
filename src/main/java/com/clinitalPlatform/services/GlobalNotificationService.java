package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.models.GlobalNotification;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.GlobalNotificationRepository;
import com.clinitalPlatform.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalNotificationService {

    @Autowired
    private GlobalNotificationRepository globalNotificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Async
    public void sendGlobalNotification(String title, String message, String description, String author , NotificationType type) {
        // Créer et enregistrer la notification globale
        GlobalNotification globalNotification = new GlobalNotification();
        globalNotification.setTitle(title);
        globalNotification.setMessage(message);
        globalNotification.setDescription(description);
        globalNotification.setAuthor(author);
        globalNotification.setType(type);

        globalNotification = globalNotificationRepository.save(globalNotification);

        // Envoyer la notification à tous les utilisateurs via WebSocket
        List<User> users = userRepository.findAll();
        for (User user : users) {
            NotificationDTO notificationDTO = NotificationDTO.builder().build();
            notificationDTO.setTitle(title);
            notificationDTO.setMessage(message);
            notificationDTO.setDescription(description);
            notificationDTO.setAutor(author);
            notificationDTO.setType(type.toString());

            // Envoi via WebSocket
            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/notifications",
                    notificationDTO
            );
        }
    }
}

