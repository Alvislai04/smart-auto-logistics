package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.AlertResponse;
import com.fyp.autologistics.dto.DashboardOverviewResponse;
import com.fyp.autologistics.dto.DashboardSummaryResponse;
import com.fyp.autologistics.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public DashboardOverviewResponse getDashboardOverview(Authentication authentication) {
        return dashboardService.getDashboardOverview(authentication.getName());
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary(Authentication authentication) {
        return dashboardService.getDashboardSummary(authentication.getName());
    }

    @GetMapping("/open-alerts")
    public List<AlertResponse> getDashboardOpenAlerts(Authentication authentication) {
        return dashboardService.getDashboardOpenAlerts(authentication.getName());
    }
}
