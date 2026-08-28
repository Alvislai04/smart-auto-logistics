package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.AlertResponse;
import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.StockAlertRule;
import com.fyp.autologistics.entity.TrackingEvent;
import com.fyp.autologistics.enums.AlertReferenceType;
import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.AlertRepository;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.TrackingEventRepository;
import com.fyp.autologistics.util.GeoDistanceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AlertService {

    private static final double IDLE_SPEED_THRESHOLD_KMH = 1.0;
    private static final double DELAY_SPEED_THRESHOLD_KMH = 15.0;
    private static final double ROUTE_DEVIATION_THRESHOLD_KM = 10.0;
    private static final double PROGRESS_SHORTFALL_TOLERANCE = 0.20;
    private static final double MIN_EXPECTED_PROGRESS_FOR_SHORTFALL_ALERT = 0.25;
    private static final long MISSING_UPDATE_THRESHOLD_MINUTES = 30;
    private static final List<AlertStatus> ACTIVE_ALERT_STATUSES = List.of(
            AlertStatus.OPEN,
            AlertStatus.ACKNOWLEDGED
    );

    private static final Set<ShipmentStatus> MONITORED_STATUSES = Set.of(
            ShipmentStatus.CREATED,
            ShipmentStatus.READY_FOR_DISPATCH,
            ShipmentStatus.PICKED_UP,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.DELAYED,
            ShipmentStatus.ARRIVED
    );

    private final AlertRepository alertRepository;
    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentService shipmentService;
    private final ShipmentEventService shipmentEventService;
    private final AuthenticatedUserService authenticatedUserService;

    public AlertService(
            AlertRepository alertRepository,
            ShipmentRepository shipmentRepository,
            TrackingEventRepository trackingEventRepository,
            ShipmentService shipmentService,
            ShipmentEventService shipmentEventService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.alertRepository = alertRepository;
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.shipmentService = shipmentService;
        this.shipmentEventService = shipmentEventService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
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
                null,
                null,
                ShipmentEventSourceType.ALERT_ENGINE
        );

        return mapToResponse(savedAlert);
    }

    @Transactional
    public AlertResponse createOrderUpdateAlert(
            Long shipmentId,
            ShipmentStatus previousStatus,
            ShipmentStatus currentStatus,
            ShipmentEventSourceType sourceType
    ) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);
        resolveActiveAlerts(
                alertRepository.findByShipmentIdAndAlertTypeAndStatusInOrderByCreatedAtDesc(
                        shipmentId,
                        AlertType.ORDER_UPDATE,
                        ACTIVE_ALERT_STATUSES
                )
        );

        Alert alert = new Alert();
        alert.setShipment(shipment);
        alert.setAlertType(AlertType.ORDER_UPDATE);
        alert.setSeverity(resolveOrderUpdateSeverity(currentStatus));
        alert.setStatus(AlertStatus.OPEN);
        alert.setTitle("Shipment order updated");
        alert.setMessage(
                shipment.getShipmentCode()
                        + " changed from "
                        + formatEnumLabel(previousStatus.name())
                        + " to "
                        + formatEnumLabel(currentStatus.name())
                        + ". Source: "
                        + formatEnumLabel(sourceType.name())
                        + "."
        );

        return mapToResponse(alertRepository.save(alert));
    }

    @Transactional
    public void synchronizeLowStockAlert(StockAlertRule rule, long currentQuantity) {
        List<Alert> activeAlerts = alertRepository
                .findByStockAlertRuleIdAndAlertTypeAndStatusInOrderByCreatedAtDesc(
                        rule.getId(),
                        AlertType.LOW_STOCK,
                        ACTIVE_ALERT_STATUSES
                );

        boolean lowStock = rule.isActive() && currentQuantity < rule.getMinimumQuantity();

        if (!lowStock) {
            resolveActiveAlerts(activeAlerts);
            return;
        }

        String title = "Low stock: " + rule.getVehicleModel().getDisplayName();
        String message = buildLowStockMessage(rule, currentQuantity);
        AlertSeverity severity = currentQuantity == 0
                ? AlertSeverity.HIGH
                : AlertSeverity.MEDIUM;

        if (!activeAlerts.isEmpty()) {
            Alert activeAlert = activeAlerts.getFirst();
            activeAlert.setSeverity(severity);
            activeAlert.setTitle(title);
            activeAlert.setMessage(message);
            alertRepository.save(activeAlert);
            return;
        }

        Alert alert = new Alert();
        alert.setStockAlertRule(rule);
        alert.setAlertType(AlertType.LOW_STOCK);
        alert.setSeverity(severity);
        alert.setStatus(AlertStatus.OPEN);
        alert.setTitle(title);
        alert.setMessage(message);
        alertRepository.save(alert);
    }

    public void evaluateShipmentMonitoringRules(Long shipmentId) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);
        evaluateShipmentMonitoringRules(shipment);
    }

    public void evaluateShipmentMonitoringRules(
            Shipment shipment,
            TrackingEvent latestTrackingEvent
    ) {
        if (!isShipmentMonitored(shipment)) {
            return;
        }

        evaluateEtaOverdue(shipment);
        evaluateMissingUpdate(shipment, Optional.ofNullable(latestTrackingEvent));

        if (latestTrackingEvent == null) {
            return;
        }

        evaluateTrackingCondition(shipment, latestTrackingEvent);
        evaluateRouteDeviation(shipment, latestTrackingEvent);
        evaluateProgressShortfall(shipment, latestTrackingEvent);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getOpenAlerts(String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);

        return findOpenAlertsForUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countOpenAlerts(String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        return countOpenAlertsForUser(user);
    }

    @Transactional
    public List<AlertResponse> getAlertsByShipmentId(Long shipmentId, String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);

        if (!canViewShipmentAlerts(user, shipment)) {
            return List.of();
        }

        evaluateShipmentMonitoringRules(shipment);

        return alertRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void evaluateShipmentMonitoringRules(Shipment shipment) {
        Optional<TrackingEvent> latestTrackingEvent =
                trackingEventRepository.findFirstByShipmentIdOrderByRecordedAtDesc(
                        shipment.getId()
                );

        if (!isShipmentMonitored(shipment)) {
            return;
        }

        evaluateEtaOverdue(shipment);
        evaluateMissingUpdate(shipment, latestTrackingEvent);

        latestTrackingEvent.ifPresent(trackingEvent -> {
            evaluateTrackingCondition(shipment, trackingEvent);
            evaluateRouteDeviation(shipment, trackingEvent);
            evaluateProgressShortfall(shipment, trackingEvent);
        });
    }

    private void evaluateEtaOverdue(Shipment shipment) {
        if (shipment.getEstimatedArrival() == null
                || !LocalDateTime.now().isAfter(shipment.getEstimatedArrival())) {
            return;
        }

        createOperationalAlert(
                shipment,
                AlertType.ETA_OVERDUE,
                AlertSeverity.HIGH,
                "ETA overdue",
                "Shipment has passed its estimated arrival time and is not delivered yet."
        );
        markShipmentDelayedIfActive(shipment);
    }

    private void evaluateMissingUpdate(
            Shipment shipment,
            Optional<TrackingEvent> latestTrackingEvent
    ) {
        LocalDateTime referenceTime = latestTrackingEvent
                .map(TrackingEvent::getRecordedAt)
                .orElse(shipment.getEstimatedDeparture());

        if (referenceTime == null || LocalDateTime.now().isBefore(referenceTime)) {
            return;
        }

        long minutesSinceUpdate = Duration.between(referenceTime, LocalDateTime.now()).toMinutes();

        if (minutesSinceUpdate < MISSING_UPDATE_THRESHOLD_MINUTES) {
            return;
        }

        createOperationalAlert(
                shipment,
                AlertType.MISSING_UPDATE,
                AlertSeverity.MEDIUM,
                "Tracking update missing",
                "No tracking update has been recorded for "
                        + minutesSinceUpdate
                        + " minutes."
        );
        markShipmentDelayedIfActive(shipment);
    }

    private void evaluateTrackingCondition(
            Shipment shipment,
            TrackingEvent trackingEvent
    ) {
        ConditionStatus conditionStatus = resolveConditionStatus(trackingEvent);

        if (conditionStatus == ConditionStatus.IDLE) {
            createDetectionEventIfAlertIsNew(
                    shipment,
                    AlertType.IDLE,
                    ShipmentEventType.IDLE_DETECTED,
                    "Idle condition detected. Vehicle speed is "
                            + trackingEvent.getSpeed()
                            + " km/h."
            );
            createOperationalAlert(
                    shipment,
                    AlertType.IDLE,
                    AlertSeverity.MEDIUM,
                    "Vehicle idle detected",
                    "The vehicle appears to be idle or not moving. Current speed: "
                            + trackingEvent.getSpeed()
                            + " km/h."
            );
            markShipmentDelayedIfActive(shipment);
            return;
        }

        if (conditionStatus == ConditionStatus.DELAYED) {
            createDetectionEventIfAlertIsNew(
                    shipment,
                    AlertType.DELAY,
                    ShipmentEventType.DELAY_DETECTED,
                    "Delay condition detected. Vehicle speed is "
                            + trackingEvent.getSpeed()
                            + " km/h."
            );
            createOperationalAlert(
                    shipment,
                    AlertType.DELAY,
                    AlertSeverity.HIGH,
                    "Possible shipment delay detected",
                    "The vehicle speed is below the expected movement threshold. Current speed: "
                            + trackingEvent.getSpeed()
                            + " km/h."
            );
            markShipmentDelayedIfActive(shipment);
            return;
        }

        if (conditionStatus == ConditionStatus.SENSOR_WARNING) {
            createOperationalAlert(
                    shipment,
                    AlertType.SENSOR_WARNING,
                    AlertSeverity.MEDIUM,
                    "Sensor warning detected",
                    "A tracking event reported a sensor warning for this shipment."
            );
        }
    }

    private void evaluateRouteDeviation(
            Shipment shipment,
            TrackingEvent trackingEvent
    ) {
        if (!hasTrackingCoordinates(trackingEvent)) {
            return;
        }

        if (trackingEvent.getConditionStatus() == ConditionStatus.ROUTE_DEVIATION) {
            createRouteDeviationAlert(
                    shipment,
                    "Tracking event reported a route deviation condition."
            );
            markShipmentDelayedIfActive(shipment);
            return;
        }

        if (!hasPlannedRouteCoordinates(shipment)) {
            return;
        }

        double deviationDistanceKm = GeoDistanceUtils.distanceFromPointToSegmentKm(
                trackingEvent.getLatitude(),
                trackingEvent.getLongitude(),
                shipment.getOriginLatitude(),
                shipment.getOriginLongitude(),
                shipment.getDestinationLatitude(),
                shipment.getDestinationLongitude()
        );

        if (deviationDistanceKm <= ROUTE_DEVIATION_THRESHOLD_KM) {
            return;
        }

        createRouteDeviationAlert(
                shipment,
                "Vehicle is "
                        + roundOneDecimal(deviationDistanceKm)
                        + " km away from the planned route."
        );
        markShipmentDelayedIfActive(shipment);
    }

    private void evaluateProgressShortfall(
            Shipment shipment,
            TrackingEvent trackingEvent
    ) {
        if (!hasPlannedRouteCoordinates(shipment)
                || !hasTrackingCoordinates(trackingEvent)
                || shipment.getEstimatedDeparture() == null
                || shipment.getEstimatedArrival() == null
                || !trackingEvent.getRecordedAt().isAfter(shipment.getEstimatedDeparture())) {
            return;
        }

        long totalTransitSeconds = Duration.between(
                shipment.getEstimatedDeparture(),
                shipment.getEstimatedArrival()
        ).toSeconds();

        if (totalTransitSeconds <= 0) {
            return;
        }

        long elapsedSeconds = Duration.between(
                shipment.getEstimatedDeparture(),
                trackingEvent.getRecordedAt()
        ).toSeconds();
        double expectedProgress = clamp((double) elapsedSeconds / totalTransitSeconds, 0.0, 1.0);

        if (expectedProgress < MIN_EXPECTED_PROGRESS_FOR_SHORTFALL_ALERT) {
            return;
        }

        double actualProgress = GeoDistanceUtils.progressRatioOnSegment(
                trackingEvent.getLatitude(),
                trackingEvent.getLongitude(),
                shipment.getOriginLatitude(),
                shipment.getOriginLongitude(),
                shipment.getDestinationLatitude(),
                shipment.getDestinationLongitude()
        );

        if (actualProgress + PROGRESS_SHORTFALL_TOLERANCE >= expectedProgress) {
            return;
        }

        createOperationalAlert(
                shipment,
                AlertType.PROGRESS_SHORTFALL,
                AlertSeverity.HIGH,
                "Route progress below plan",
                "Shipment progress is behind the planned schedule. Expected "
                        + formatPercentage(expectedProgress)
                        + " completion, actual "
                        + formatPercentage(actualProgress)
                        + "."
        );
        markShipmentDelayedIfActive(shipment);
    }

    @Transactional
    public AlertResponse acknowledgeAlert(Long alertId, String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        Alert alert = findAlertEntityById(alertId);
        validateUserCanAcknowledgeAlert(user, alert);

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            return mapToResponse(alert);
        }

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());

        Alert savedAlert = alertRepository.save(alert);
        return mapToResponse(savedAlert);
    }

    @Transactional
    public AlertResponse resolveAlert(Long alertId, String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        Alert alert = findAlertEntityById(alertId);
        validateUserCanResolveAlert(user, alert);

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

    private void createOperationalAlert(
            Shipment shipment,
            AlertType alertType,
            AlertSeverity severity,
            String title,
            String message
    ) {
        createAlertIfNotOpen(shipment.getId(), alertType, severity, title, message);
    }

    private void createRouteDeviationAlert(Shipment shipment, String message) {
        createOperationalAlert(
                shipment,
                AlertType.ROUTE_DEVIATION,
                AlertSeverity.HIGH,
                "Route deviation detected",
                message
        );
    }

    private void createDetectionEventIfAlertIsNew(
            Shipment shipment,
            AlertType alertType,
            ShipmentEventType eventType,
            String description
    ) {
        if (hasOpenAlert(shipment.getId(), alertType)) {
            return;
        }

        shipmentEventService.createEventForShipment(
                shipment,
                eventType,
                description,
                null,
                null,
                null,
                ShipmentEventSourceType.ALERT_ENGINE
        );
    }

    private boolean hasOpenAlert(Long shipmentId, AlertType alertType) {
        return alertRepository.existsByShipmentIdAndAlertTypeAndStatus(
                shipmentId,
                alertType,
                AlertStatus.OPEN
        );
    }

    private boolean isShipmentMonitored(Shipment shipment) {
        return MONITORED_STATUSES.contains(shipment.getStatus());
    }

    private ConditionStatus resolveConditionStatus(TrackingEvent trackingEvent) {
        if (trackingEvent.getConditionStatus() != null
                && trackingEvent.getConditionStatus() != ConditionStatus.NORMAL) {
            return trackingEvent.getConditionStatus();
        }

        Double speed = trackingEvent.getSpeed();

        if (speed == null) {
            return ConditionStatus.NORMAL;
        }

        if (speed <= IDLE_SPEED_THRESHOLD_KMH) {
            return ConditionStatus.IDLE;
        }

        if (speed < DELAY_SPEED_THRESHOLD_KMH) {
            return ConditionStatus.DELAYED;
        }

        return ConditionStatus.NORMAL;
    }

    private boolean hasPlannedRouteCoordinates(Shipment shipment) {
        return shipment.getOriginLatitude() != null
                && shipment.getOriginLongitude() != null
                && shipment.getDestinationLatitude() != null
                && shipment.getDestinationLongitude() != null;
    }

    private boolean hasTrackingCoordinates(TrackingEvent trackingEvent) {
        return trackingEvent.getLatitude() != null
                && trackingEvent.getLongitude() != null;
    }

    private void markShipmentDelayedIfActive(Shipment shipment) {
        if (!isShipmentMonitored(shipment) || shipment.getStatus() == ShipmentStatus.DELAYED) {
            return;
        }

        shipmentService.updateSystemStatus(
                shipment,
                ShipmentStatus.DELAYED,
                shipment.getCurrentStage(),
                ShipmentEventSourceType.ALERT_ENGINE,
                "Monitoring rules marked the shipment as delayed."
        );
    }

    private String formatPercentage(double value) {
        return Math.round(value * 100) + "%";
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private List<Alert> findOpenAlertsForUser(AppUser user) {
        if (user.getRole() == UserRole.MANUFACTURER) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.findVisibleToManufacturer(
                            AlertStatus.OPEN,
                            partyProfileKey,
                            Pageable.unpaged()
                    )
                    : List.of();
        }

        if (user.getRole() == UserRole.DEALERSHIP) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.findVisibleToDealership(
                            AlertStatus.OPEN,
                            partyProfileKey,
                            Pageable.unpaged()
                    )
                    : List.of();
        }

        if (user.getRole() == UserRole.LOGISTICS_PROVIDER) {
            return alertRepository.findByStatusAndShipmentIsNotNullOrderByCreatedAtDesc(
                    AlertStatus.OPEN,
                    Pageable.unpaged()
            );
        }

        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN);
    }

    private long countOpenAlertsForUser(AppUser user) {
        if (user.getRole() == UserRole.MANUFACTURER) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.countVisibleToManufacturer(
                            AlertStatus.OPEN,
                            partyProfileKey
                    )
                    : 0;
        }

        if (user.getRole() == UserRole.DEALERSHIP) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.countVisibleToDealership(
                            AlertStatus.OPEN,
                            partyProfileKey
                    )
                    : 0;
        }

        if (user.getRole() == UserRole.LOGISTICS_PROVIDER) {
            return alertRepository.countByStatusAndShipmentIsNotNull(AlertStatus.OPEN);
        }

        return alertRepository.countByStatus(AlertStatus.OPEN);
    }

    private void validateUserCanAcknowledgeAlert(AppUser user, Alert alert) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.LOGISTICS_PROVIDER
                && alert.getShipment() != null) {
            return;
        }

        if (user.getRole() == UserRole.DEALERSHIP && canViewAlert(user, alert)) {
            return;
        }

        if (user.getRole() == UserRole.MANUFACTURER
                && (alert.getAlertType() == AlertType.ORDER_UPDATE
                    || alert.getAlertType() == AlertType.LOW_STOCK)
                && canViewAlert(user, alert)) {
            return;
        }

        throw new BadRequestException("You are not allowed to acknowledge this alert.");
    }

    private void validateUserCanResolveAlert(AppUser user, Alert alert) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.LOGISTICS_PROVIDER
                && alert.getShipment() != null) {
            return;
        }

        throw new BadRequestException("You are not allowed to resolve this alert.");
    }

    private boolean canViewShipmentAlerts(AppUser user, Shipment shipment) {
        if (shipment == null) {
            return false;
        }

        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.LOGISTICS_PROVIDER) {
            return true;
        }

        String partyProfileKey = user.getPartyProfileKey();

        if (!StringUtils.hasText(partyProfileKey)) {
            return false;
        }

        if (user.getRole() == UserRole.MANUFACTURER) {
            return partyProfileKey.equals(shipment.getManufacturerKey());
        }

        if (user.getRole() == UserRole.DEALERSHIP) {
            return partyProfileKey.equals(shipment.getDealershipKey());
        }

        return false;
    }

    private boolean canViewAlert(AppUser user, Alert alert) {
        if (alert.getShipment() != null) {
            return canViewShipmentAlerts(user, alert.getShipment());
        }

        if (alert.getStockAlertRule() == null) {
            return false;
        }

        return StringUtils.hasText(user.getPartyProfileKey())
                && user.getPartyProfileKey().equals(
                        alert.getStockAlertRule().getPartyProfile().getPartyKey()
                );
    }

    private Alert findAlertEntityById(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
    }

    private AlertResponse mapToResponse(Alert alert) {
        Shipment shipment = alert.getShipment();
        StockAlertRule stockAlertRule = alert.getStockAlertRule();
        AlertReferenceType referenceType = shipment != null
                ? AlertReferenceType.SHIPMENT
                : AlertReferenceType.STOCK_RULE;
        Long referenceId = shipment != null ? shipment.getId() : stockAlertRule.getId();
        String referenceLabel = shipment != null
                ? shipment.getShipmentCode()
                : stockAlertRule.getRuleKey();

        return new AlertResponse(
                alert.getId(),
                shipment == null ? null : shipment.getId(),
                shipment == null ? null : shipment.getShipmentCode(),
                referenceType,
                referenceId,
                referenceLabel,
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

    private void resolveActiveAlerts(List<Alert> activeAlerts) {
        if (activeAlerts.isEmpty()) {
            return;
        }

        LocalDateTime resolvedAt = LocalDateTime.now();
        activeAlerts.forEach(alert -> {
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(resolvedAt);
        });
        alertRepository.saveAll(activeAlerts);
    }

    private String buildLowStockMessage(StockAlertRule rule, long currentQuantity) {
        return rule.getPartyProfile().getCompanyName()
                + " has "
                + currentQuantity
                + " vehicle(s) for "
                + rule.getVehicleModel().getDisplayName()
                + " at "
                + formatEnumLabel(rule.getLogisticsStage().name())
                + ". Minimum required: "
                + rule.getMinimumQuantity()
                + ".";
    }

    private AlertSeverity resolveOrderUpdateSeverity(ShipmentStatus currentStatus) {
        if (currentStatus == ShipmentStatus.DELAYED) {
            return AlertSeverity.HIGH;
        }

        if (currentStatus == ShipmentStatus.CANCELLED) {
            return AlertSeverity.MEDIUM;
        }

        return AlertSeverity.LOW;
    }

    private String formatEnumLabel(String value) {
        String normalized = value.replace('_', ' ').toLowerCase();
        StringBuilder result = new StringBuilder(normalized.length());
        boolean capitalizeNext = true;

        for (char character : normalized.toCharArray()) {
            result.append(capitalizeNext ? Character.toUpperCase(character) : character);
            capitalizeNext = character == ' ';
        }

        return result.toString();
    }
}
