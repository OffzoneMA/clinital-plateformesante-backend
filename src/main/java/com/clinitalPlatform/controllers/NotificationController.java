package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.services.GlobalNotificationService;
import com.clinitalPlatform.services.NotificationService;
import com.clinitalPlatform.services.PushNotificationService;
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
        pushNotificationService.sendAppointmentReminder(userId, message , appointmentDetails ,autor, rdvStart , rdvId);
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
        pushNotificationService.sendAppointmentCancellation(userId, message , appointmentDetails , autor , rdvStart , rdvId);
        return ResponseEntity.ok().build();
    }
}