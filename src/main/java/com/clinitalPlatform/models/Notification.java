package com.clinitalPlatform.models;

import java.util.Date;

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
	private Date createdAt;
	private String url;
	private boolean isRead;
	@ManyToOne
	@JoinColumn(name = "id")
	private User user;

	public Notification() {
	}

}
