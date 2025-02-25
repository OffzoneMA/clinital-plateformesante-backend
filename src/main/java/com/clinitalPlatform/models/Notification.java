package com.clinitalPlatform.models;

import java.time.LocalDateTime;
import java.util.Date;

import com.clinitalPlatform.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_notif;
	private String title;
	private String message;
	private String description;
	private String autor;
	private boolean requiresAction;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;
	private LocalDateTime createdAt;
	private LocalDateTime rdvStart;
	private String url;
	private boolean isRead;
	@ManyToOne
	@JoinColumn(name = "id")
	private User user;

	public Notification() {
	}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		if (type == null) {
			type = NotificationType.INFO;
		}
	}

}
