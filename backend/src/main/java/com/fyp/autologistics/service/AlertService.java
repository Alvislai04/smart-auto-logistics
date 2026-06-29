package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.AlertResponse;
import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.AlertRepository;
import com.fyp.autologistics.enums.ShipmentEventType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final ShipmentService shipmentService;
    private final ShipmentEventService shipmentEventService;

    public AlertService(
            AlertRepository alertRepository,
            ShipmentService shipmentService,
            ShipmentEventService shipmentEventService
    ) {
        this.alertRepository = alertRepository;
        this.shipmentService = shipmentService;
        this.shipmentEventService = shipmentEventService;
    }

    public AlertResponse createAlertIfNotOpen(
            Long shipmentId,
            AlertType alertType,
            AlertSeverity severity,
            String title,
            String message
    ) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);

        boolean openAlertExists = alertRepository.existsByShipmentIdAndAlertTypeAndStatus(
                shipmentId,
                alertType,
                AlertStatus.OPEN
        );

        if (openAlertExists) {
            return getLatestOpenAlertByShipmentAndType(shipmentId, alertType);
        }

        Alert alert = new Alert();
        alert.setShipment(shipment);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setStatus(AlertStatus.OPEN);
        alert.setTitle(title);
        alert.setMessage(message);

        Alert savedAlert = alertRepository.save(alert);

        shipmentEventService.createEventForShipment(
                shipment,
                ShipmentEventType.ALERT_CREATED,
                "Alert created: " + title,
                null,
                null
        );

return mapToResponse(savedAlert);
    }

    public List<AlertResponse> getAllOpenAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AlertResponse> getAlertsByShipmentId(Long shipmentId) {
        shipmentService.findShipmentEntityById(shipmentId);

        return alertRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AlertResponse acknowledgeAlert(Long alertId) {
        Alert alert = findAlertEntityById(alertId);

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            return mapToResponse(alert);
        }

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());

        Alert savedAlert = alertRepository.save(alert);
        return mapToResponse(savedAlert);
    }

    public AlertResponse resolveAlert(Long alertId) {
        Alert alert = findAlertEntityById(alertId);

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());

        Alert savedAlert = alertRepository.save(alert);
        return mapToResponse(savedAlert);
    }

    private AlertResponse getLatestOpenAlertByShipmentAndType(Long shipmentId, AlertType alertType) {
        return alertRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .filter(alert -> alert.getAlertType() == alertType)
                .filter(alert -> alert.getStatus() == AlertStatus.OPEN)
                .findFirst()
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Open alert not found"));
    }

    private Alert findAlertEntityById(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
    }

    private AlertResponse mapToResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getShipment().getId(),
                alert.getShipment().getShipmentCode(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt()
        );
    }
}