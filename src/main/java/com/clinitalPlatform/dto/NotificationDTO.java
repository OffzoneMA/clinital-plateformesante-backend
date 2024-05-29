package com.clinitalPlatform.dto;

import java.util.Date;

import lombok.Data;

@Data
public class NotificationDTO {

	private Long id_notif;
	private String title;
	private String message;
	private Date createdAt;
	private String url;
	private boolean isRead;

	private UserDTO user;

}
