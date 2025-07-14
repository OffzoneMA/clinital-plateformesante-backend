package com.clinitalPlatform.payload.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private String title;
    private String message;
    private String description;
    private String type;
    private boolean isRead;
    private String url;
}
