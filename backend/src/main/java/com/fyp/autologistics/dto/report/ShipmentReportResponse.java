package com.fyp.autologistics.dto.report;

import java.time.LocalDateTime;
import java.util.List;

public record ShipmentReportResponse(
        LocalDateTime generatedAt,
        String scopeLabel,
        long totalShipments,
        long activeShipments,
        long delayedShipments,
        long deliveredShipments,
        long cancelledShipments,
        double deliveryCompletionRate,
        double delayRate,
        List<ReportMetricResponse> statusBreakdown,
        List<ReportMetricResponse> stageBreakdown,
        List<ShipmentReportRowResponse> shipments
) {
}
