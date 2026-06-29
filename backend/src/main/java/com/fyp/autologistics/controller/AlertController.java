package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.AlertResponse;
import com.fyp.autologistics.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:5173")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/open")
    public List<AlertResponse> getAllOpenAlerts() {
        return alertService.getAllOpenAlerts();
    }

    @GetMapping("/shipment/{shipmentId}")
    public List<AlertResponse> getAlertsByShipmentId(@PathVariable Long shipmentId) {
        return alertService.getAlertsByShipmentId(shipmentId);
    }

    @PutMapping("/{alertId}/acknowledge")
    public AlertResponse acknowledgeAlert(@PathVariable Long alertId) {
        return alertService.acknowledgeAlert(alertId);
    }

    @PutMapping("/{alertId}/resolve")
    public AlertResponse resolveAlert(@PathVariable Long alertId) {
        return alertService.resolveAlert(alertId);
    }
}