package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.CompanyNotificationStatus;
import com.fyp.autologistics.enums.CompanyNotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_notifications")
public class CompanyNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyNotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyNotificationStatus status;

    @Column(nullable = false)
    private String recipientCompany;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1500)
    private String message;

    private String referenceType;

    private Long referenceId;

    private LocalDateTime sentAt;

    public CompanyNotification() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = CompanyNotificationStatus.SENT;
        }
    }

    public Long getId() {
        return id;
    }

    public CompanyNotificationType getType() {
        return type;
    }

    public void setType(CompanyNotificationType type) {
        this.type = type;
    }

    public CompanyNotificationStatus getStatus() {
        return status;
    }

    public void setStatus(CompanyNotificationStatus status) {
        this.status = status;
    }

    public String getRecipientCompany() {
        return recipientCompany;
    }

    public void setRecipientCompany(String recipientCompany) {
        this.recipientCompany = recipientCompany;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}