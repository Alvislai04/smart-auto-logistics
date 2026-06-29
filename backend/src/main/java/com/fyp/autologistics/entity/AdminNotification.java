package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.AdminNotificationStatus;
import com.fyp.autologistics.enums.AdminNotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notifications")
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminNotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminNotificationStatus status;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1500)
    private String message;

    private String referenceType;

    private Long referenceId;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public AdminNotification() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = AdminNotificationStatus.UNREAD;
        }
    }

    public Long getId() {
        return id;
    }

    public AdminNotificationType getType() {
        return type;
    }

    public void setType(AdminNotificationType type) {
        this.type = type;
    }

    public AdminNotificationStatus getStatus() {
        return status;
    }

    public void setStatus(AdminNotificationStatus status) {
        this.status = status;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}