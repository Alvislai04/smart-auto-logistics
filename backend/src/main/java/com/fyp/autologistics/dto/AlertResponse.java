package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;

import java.time.LocalDateTime;

public class AlertResponse {

    private Long id;
    private Long shipmentId;
    private String shipmentCode;
    private AlertType alertType;
    private AlertSeverity severity;
    private AlertStatus status;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;

    public AlertResponse(
            Long id,
            Long shipmentId,
            String shipmentCode,
            AlertType alertType,
            AlertSeverity severity,
            AlertStatus status,
            String title,
            String message,
            LocalDateTime createdAt,
            LocalDateTime acknowledgedAt,
            LocalDateTime resolvedAt
    ) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.shipmentCode = shipmentCode;
        this.alertType = alertType;
        this.severity = severity;
        this.status = status;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public String getShipmentCode() {
        return shipmentCode;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}