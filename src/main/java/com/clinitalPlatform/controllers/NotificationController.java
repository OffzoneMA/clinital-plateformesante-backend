package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.repository.NotificationRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.services.GlobalNotificationService;
import com.clinitalPlatform.services.NotificationService;
import com.clinitalPlatform.services.PushNotificationService;
import com.clinitalPlatform.util.GlobalVariables;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private GlobalNotificationService globalNotificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private GlobalVariables globalVariables;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/since")
    public ResponseEntity<List<NotificationDTO>> getNotificationsSince(
            @PathVariable Long userId,
            @RequestParam LocalDateTime since) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId, since));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByType(
            @PathVariable Long userId,
            @PathVariable NotificationType type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(userId, type));
    }

    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try{
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try{
            notificationService.deleteNotification(notificationId, userId);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

    }

    /**
     * Supprimer toutes les notifications lues d'un utilisateur.
     */
    @DeleteMapping("/delete-read/{id}")
    @Transactional
    public ResponseEntity<?> deleteAllReadNotifications(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("L'ID utilisateur est invalide.");
            }

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            notificationRepository.deleteAllReadByUser(user);

            return ResponseEntity.ok("Notifications lues supprimées avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression des notifications : " + e.getMessage());
        }
    }


    // Endpoints pour l'envoi de notifications

    @PostMapping("/send/user/{userId}")
    public ResponseEntity<Void> sendNotificationToUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> notificationData) {
        pushNotificationService.sendNotificationToUser(
                userId,
                (String) notificationData.get("title"),
                (String) notificationData.get("message"),
                (String) notificationData.get("description"),
                (String) notificationData.get("autor"),
                NotificationType.valueOf((String) notificationData.get("type")),
                (Boolean) notificationData.get("requiresAction"),
                (String) notificationData.get("url") ,
                null,
                null ,
                null
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/broadcast")
    public ResponseEntity<Void> sendBroadcastNotification(
            @RequestBody Map<String, Object> notificationData) {
        globalNotificationService.sendGlobalNotification(
                (String) notificationData.get("title"),
                (String) notificationData.get("message"),
                (String) notificationData.get("description"),
                (String) notificationData.get("autor"),
                NotificationType.valueOf((String) notificationData.get("type"))
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/appointment-reminder/{userId}")
    public ResponseEntity<Void> sendAppointmentReminder(
            @PathVariable Long userId,
            @RequestBody String message ,
            @RequestBody String appointmentDetails ,
            @RequestBody String autor ,
            @RequestBody LocalDateTime rdvStart ,
            @RequestBody Long rdvId) {
        pushNotificationService.sendAppointmentReminder(userId, message , appointmentDetails ,autor, rdvStart , rdvId , null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/appointment-cancellation/{userId}")
    public ResponseEntity<Void> sendAppointmentCancellation(
            @PathVariable Long userId,
            @RequestBody String message ,
            @RequestBody String appointmentDetails ,
            @RequestBody String autor ,
            @RequestBody LocalDateTime rdvStart ,
            @RequestBody Long rdvId) {
        pushNotificationService.sendAppointmentCancellation(userId, "Votre rendez-vous à été annulé" , message , appointmentDetails , autor , rdvStart , rdvId , null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/document-notification")
    public ResponseEntity<?> sendDocumentNotification(
            @RequestBody Map<String, Object> notificationData) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();

            pushNotificationService.sendDocumentNotification(
                    userId,
                    (String) notificationData.get("title"),
                    (String) notificationData.get("message"),
                    (String) notificationData.get("description"),
                    notificationData.get("documentId") != null
                            ? ((Number) notificationData.get("documentId")).longValue()
                            : null,
                    NotificationType.valueOf((String) notificationData.get("type")),
                    (String) notificationData.get("docType") ,
                    (String) notificationData.get("autor")
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de la notification : ", e);
            return ResponseEntity.status(500).body(new ApiResponse(false, "Erreur lors de l'envoi de la notification : " + e.getMessage()));
        }
    }
}