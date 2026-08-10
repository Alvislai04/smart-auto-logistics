package com.fyp.autologistics.dto.report;

import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;

import java.time.LocalDateTime;

public record AlertReportRowResponse(
        Long alertId,
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
}
