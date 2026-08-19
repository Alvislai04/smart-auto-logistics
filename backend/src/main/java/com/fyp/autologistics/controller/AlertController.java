package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.AlertResponse;
import com.fyp.autologistics.dto.OpenAlertCountResponse;
import com.fyp.autologistics.service.AlertService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/open")
    public List<AlertResponse> getOpenAlerts(Authentication authentication) {
        return alertService.getOpenAlerts(authentication.getName());
    }

    @GetMapping("/open-count")
    public OpenAlertCountResponse getOpenAlertCount(Authentication authentication) {
        return new OpenAlertCountResponse(
                alertService.countOpenAlerts(authentication.getName())
        );
    }

    @GetMapping("/shipment/{shipmentId}")
    public List<AlertResponse> getAlertsByShipmentId(
            @PathVariable Long shipmentId,
            Authentication authentication
    ) {
        return alertService.getAlertsByShipmentId(shipmentId, authentication.getName());
    }

    @PutMapping("/{alertId}/acknowledge")
    public AlertResponse acknowledgeAlert(
            @PathVariable Long alertId,
            Authentication authentication
    ) {
        return alertService.acknowledgeAlert(alertId, authentication.getName());
    }

    @PutMapping("/{alertId}/resolve")
    public AlertResponse resolveAlert(
            @PathVariable Long alertId,
            Authentication authentication
    ) {
        return alertService.resolveAlert(alertId, authentication.getName());
    }
}
