package com.fyp.autologistics.dto.report;

import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentReportRowResponse(
        Long shipmentId,
        String shipmentCode,
        String vehicleVin,
        String vehicleModel,
        String origin,
        String destination,
        String dealershipName,
        ShipmentStatus status,
        LogisticsStage currentStage,
        LocalDateTime estimatedDeparture,
        LocalDateTime estimatedArrival,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime receiptConfirmedAt
) {
}
