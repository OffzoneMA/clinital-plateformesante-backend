package com.clinitalPlatform;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.Notification;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.NotificationRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        notification = new Notification();
        notification.setId_notif(100L);
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test notification");
        notification.setAutor("Dr Ducobut John");
        notification.setType(NotificationType.REMINDER);
        notification.setUser(user);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
    }

    @Test
    void testGetUserNotifications() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(notification));

        List<NotificationDTO> notifications = notificationService.getUserNotifications(1L);

        assertFalse(notifications.isEmpty());
        assertEquals(1, notifications.size());
        assertEquals("Test Notification", notifications.get(0).getTitle());
    }

    @Test
    void testGetUserNotifications_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getUserNotifications(1L));
    }

    @Test
    void testGetUnreadCount() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserAndIsReadFalse(user)).thenReturn(5L);

        long unreadCount = notificationService.getUnreadCount(1L);

        assertEquals(5, unreadCount);
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(notification));

        notificationService.markAsRead(100L, 1L);

        verify(notificationRepository, times(1)).markAsRead(notification.getId_notif());
    }

    @Test
    void testMarkAsRead_NotAllowed() {
        User user1 = new User();
        user1.setId(2L);
        notification.setUser(user1); // Une autre personne

        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(notification));

        Exception exception = assertThrows(Exception.class, () -> notificationService.markAsRead(100L, 1L));
        assertEquals("You are not allowed to modify this notification", exception.getMessage());
    }

    @Test
    void testDeleteNotification_Success() throws Exception {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(100L, 1L);

        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    void testDeleteNotification_NotAllowed() {
        User user2 = new User();
        user2.setId(3L);
        notification.setUser(user2); // Notification d'un autre utilisateur

        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(notification));

        Exception exception = assertThrows(Exception.class, () -> notificationService.deleteNotification(100L, 1L));
        assertEquals("You can't delete this notification", exception.getMessage());
    }

    @Test
    void testGetNotificationsByType() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(eq(user), any()))
                .thenReturn(Arrays.asList(notification));

        List<NotificationDTO> notifications = notificationService.getNotificationsByType(1L, NotificationType.INFO);

        assertFalse(notifications.isEmpty());
        assertEquals(1, notifications.size());
        assertEquals("Test Notification", notifications.get(0).getTitle());
    }
}

