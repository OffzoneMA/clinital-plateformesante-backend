package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.Notification;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.NotificationRepository;
import com.clinitalPlatform.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository ;

    public List<NotificationDTO> getNotificationsByUser(Long userId, LocalDateTime since) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" , "By id : " + userId, "" ));

        return notificationRepository
                .findByUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les notifications d'un utilisateur
     */
    public List<NotificationDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Récupère le nombre de notifications non lues pour un utilisateur
     */
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Marque une notification comme lue
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) throws Exception {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found", "id", notificationId.toString()));

        if (!notification.getUser().getId().equals(userId)) {
            throw new Exception("You are not allowed to modify this notification");
        }


        notificationRepository.markAsRead(notification.getId_notif());
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        notificationRepository.markAllAsRead(user);
    }

    /**
     * Supprime une notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) throws Exception {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found", "id", notificationId.toString()));

        if (!notification.getUser().getId().equals(userId)) {
            throw new Exception("You can't delete this notification");
        }


        notificationRepository.delete(notification);
    }

    /**
     * Récupère les notifications filtrées par type
     */
    public List<NotificationDTO> getNotificationsByType(Long userId, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        return notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id_notif(notification.getId_notif())
                .title(notification.getTitle())
                .autor(notification.getAutor())
                .message(notification.getMessage())
                .description(notification.getDescription())
                .type(notification.getType().toString())
                .requiresAction(notification.isRequiresAction())
                .time(notification.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .rdvTime(notification.getRdvStart() != null ? notification.getRdvStart().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .url(notification.getUrl())
                .build();
    }
}
