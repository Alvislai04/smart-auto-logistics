package com.fyp.autologistics.dto.report;

public record ReportMetricResponse(
        String key,
        String label,
        long value
) {
}
