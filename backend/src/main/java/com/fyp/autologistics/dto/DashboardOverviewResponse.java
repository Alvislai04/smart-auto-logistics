package com.fyp.autologistics.dto;

import java.util.List;

public class DashboardOverviewResponse {

    private DashboardSummaryResponse summary;
    private List<AlertResponse> openAlerts;

    public DashboardOverviewResponse(
            DashboardSummaryResponse summary,
            List<AlertResponse> openAlerts
    ) {
        this.summary = summary;
        this.openAlerts = openAlerts;
    }

    public DashboardSummaryResponse getSummary() {
        return summary;
    }

    public List<AlertResponse> getOpenAlerts() {
        return openAlerts;
    }
}
