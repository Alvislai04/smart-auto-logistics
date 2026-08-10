package com.fyp.autologistics.dto.report;

import java.util.List;

public record ReportOptionsResponse(
        List<ReportOptionResponse> shipmentStatuses,
        List<ReportOptionResponse> logisticsStages,
        List<ReportOptionResponse> alertTypes,
        List<ReportOptionResponse> alertStatuses,
        List<ReportOptionResponse> alertSeverities
) {
}
