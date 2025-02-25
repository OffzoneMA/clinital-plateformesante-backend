package com.clinitalPlatform.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDTO {
	private Long id_notif;
	private String title;
	private String message;
	private String description;
	private LocalDateTime createdAt;
	private String autor;
	private String type;
	private boolean requiresAction;
	private String time;
	private String rdvTime;
	private boolean isRead;
	private String url;

	private UserDTO user;

}
