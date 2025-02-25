package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class GlobalNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String description;
    private String author;

    @Enumerated
    private NotificationType type;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection
    private Set<Long> readByUsers = new HashSet<>();  // Les utilisateurs qui ont vu cette notification.

    public GlobalNotification(Long id, String title, String message, String description,
                              String author, LocalDateTime createdAt, Set<Long> readByUsers) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.description = description;
        this.author = author;
        this.createdAt = createdAt;
        this.readByUsers = readByUsers;
    }

    public GlobalNotification() {

    }
}

