package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.AdminNotificationStatus;
import com.fyp.autologistics.enums.AdminNotificationType;

import java.time.LocalDateTime;

public class AdminNotificationResponse {

    private Long id;
    private AdminNotificationType type;
    private AdminNotificationStatus status;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public AdminNotificationResponse(
            Long id,
            AdminNotificationType type,
            AdminNotificationStatus status,
            String title,
            String message,
            String referenceType,
            Long referenceId,
            LocalDateTime createdAt,
            LocalDateTime readAt
    ) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public Long getId() {
        return id;
    }

    public AdminNotificationType getType() {
        return type;
    }

    public AdminNotificationStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }
}