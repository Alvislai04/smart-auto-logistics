package com.fyp.autologistics.dto.report;

import java.time.LocalDateTime;
import java.util.List;

public record AlertReportResponse(
        LocalDateTime generatedAt,
        String scopeLabel,
        long totalAlerts,
        long openAlerts,
        long acknowledgedAlerts,
        long resolvedAlerts,
        long highSeverityAlerts,
        List<ReportMetricResponse> typeBreakdown,
        List<ReportMetricResponse> severityBreakdown,
        List<ReportMetricResponse> statusBreakdown,
        List<AlertReportRowResponse> alerts
) {
}
