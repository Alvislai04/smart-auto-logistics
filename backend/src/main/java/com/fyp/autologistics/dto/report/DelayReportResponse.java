package com.fyp.autologistics.dto.report;

import java.time.LocalDateTime;
import java.util.List;

public record DelayReportResponse(
        LocalDateTime generatedAt,
        String scopeLabel,
        long totalDelayRecords,
        long openDelayAlerts,
        long resolvedDelayAlerts,
        long highSeverityDelayAlerts,
        List<ReportMetricResponse> delayTypeBreakdown,
        List<ReportMetricResponse> severityBreakdown,
        List<DelayReportRowResponse> delays
) {
}
