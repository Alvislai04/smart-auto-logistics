package com.fyp.autologistics.dto.report;

import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;

import java.time.LocalDateTime;

public record DelayReportRowResponse(
        Long shipmentId,
        String shipmentCode,
        String vehicleVin,
        String origin,
        String destination,
        ShipmentStatus shipmentStatus,
        LogisticsStage currentStage,
        LocalDateTime estimatedDeparture,
        LocalDateTime estimatedArrival,
        AlertType alertType,
        AlertSeverity severity,
        AlertStatus alertStatus,
        String reason,
        LocalDateTime detectedAt
) {
}
