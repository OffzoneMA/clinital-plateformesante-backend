package com.clinitalPlatform.repository;

import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.models.Notification;
import com.clinitalPlatform.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification , Long> {
    List<Notification> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
            User user,
            LocalDateTime since
    );

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id_notif = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user")
    void markAllAsRead(@Param("user") User user);


    long countByUserAndIsReadFalse(User user);

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :threshold")
    void deleteOldNotifications(@Param("threshold") LocalDateTime threshold);

    boolean existsByRdvIdAndType(Long id, NotificationType notificationType);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.isRead = true")
    void deleteAllReadByUser(@Param("user") User user);

}
