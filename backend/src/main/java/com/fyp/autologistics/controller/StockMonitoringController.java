package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.StockAlertRuleRequest;
import com.fyp.autologistics.dto.StockAlertRuleResponse;
import com.fyp.autologistics.dto.StockMonitoringOverviewResponse;
import com.fyp.autologistics.service.StockMonitoringService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-monitoring")
public class StockMonitoringController {

    private final StockMonitoringService stockMonitoringService;

    public StockMonitoringController(StockMonitoringService stockMonitoringService) {
        this.stockMonitoringService = stockMonitoringService;
    }

    @GetMapping
    public StockMonitoringOverviewResponse getOverview(Authentication authentication) {
        return stockMonitoringService.getOverview(authentication.getName());
    }

    @PostMapping("/rules")
    public StockAlertRuleResponse createRule(
            @Valid @RequestBody StockAlertRuleRequest request,
            Authentication authentication
    ) {
        return stockMonitoringService.createRule(request, authentication.getName());
    }

    @PutMapping("/rules/{ruleId}")
    public StockAlertRuleResponse updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody StockAlertRuleRequest request,
            Authentication authentication
    ) {
        return stockMonitoringService.updateRule(ruleId, request, authentication.getName());
    }
}
