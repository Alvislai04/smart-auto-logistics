package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.CompanyNotificationStatus;
import com.fyp.autologistics.enums.CompanyNotificationType;

import java.time.LocalDateTime;

public class CompanyNotificationResponse {

    private Long id;
    private CompanyNotificationType type;
    private CompanyNotificationStatus status;
    private String recipientCompany;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime sentAt;

    public CompanyNotificationResponse(
            Long id,
            CompanyNotificationType type,
            CompanyNotificationStatus status,
            String recipientCompany,
            String title,
            String message,
            String referenceType,
            Long referenceId,
            LocalDateTime sentAt
    ) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.recipientCompany = recipientCompany;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public CompanyNotificationType getType() {
        return type;
    }

    public CompanyNotificationStatus getStatus() {
        return status;
    }

    public String getRecipientCompany() {
        return recipientCompany;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}