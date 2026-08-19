package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.AlertResponse;
import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.StockAlertRule;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.repository.AlertRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.TrackingEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertServiceTests {

    private final AlertRepository alertRepository = mock(AlertRepository.class);
    private final ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
    private final TrackingEventRepository trackingEventRepository =
            mock(TrackingEventRepository.class);
    private final ShipmentService shipmentService = mock(ShipmentService.class);
    private final ShipmentEventService shipmentEventService = mock(ShipmentEventService.class);
    private final AuthenticatedUserService authenticatedUserService =
            mock(AuthenticatedUserService.class);

    private final AlertService alertService = new AlertService(
            alertRepository,
            shipmentRepository,
            trackingEventRepository,
            shipmentService,
            shipmentEventService,
            authenticatedUserService
    );

    @Test
    void manufacturerOpenAlertsAreScopedByPartyProfileKey() {
        AppUser user = buildUser(UserRole.MANUFACTURER, "MFG-PEKAN");
        Alert alert = buildAlert("SHP-001", "MFG-PEKAN", "DLR-KL");

        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(user);
        when(alertRepository.findVisibleToManufacturer(
                AlertStatus.OPEN,
                "MFG-PEKAN",
                Pageable.unpaged()
        )).thenReturn(List.of(alert));

        List<AlertResponse> alerts = alertService.getOpenAlerts("manufacturer@example.com");

        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getShipmentCode()).isEqualTo("SHP-001");
        verify(alertRepository).findVisibleToManufacturer(
                AlertStatus.OPEN,
                "MFG-PEKAN",
                Pageable.unpaged()
        );
        verify(alertRepository, never()).findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN);
    }

    @Test
    void manufacturerCannotAcknowledgeAlert() {
        AppUser user = buildUser(UserRole.MANUFACTURER, "MFG-PEKAN");
        Alert alert = buildAlert("SHP-001", "MFG-PEKAN", "DLR-KL");

        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(user);
        when(alertRepository.findById(99L)).thenReturn(Optional.of(alert));

        assertThatThrownBy(
                () -> alertService.acknowledgeAlert(99L, "manufacturer@example.com")
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You are not allowed to acknowledge this alert.");

        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void dealershipCanAcknowledgeOwnAlert() {
        AppUser user = buildUser(UserRole.DEALERSHIP, "DLR-KL");
        Alert alert = buildAlert("SHP-001", "MFG-PEKAN", "DLR-KL");

        when(authenticatedUserService.getAuthenticatedUser("dealer@example.com"))
                .thenReturn(user);
        when(alertRepository.findById(99L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponse response = alertService.acknowledgeAlert(99L, "dealer@example.com");

        assertThat(response.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(alert.getAcknowledgedAt()).isNotNull();
        verify(alertRepository).save(alert);
    }

    @Test
    void manufacturerCanAcknowledgeOwnLowStockAlert() {
        AppUser user = buildUser(UserRole.MANUFACTURER, "MFG-PEKAN");
        Alert alert = buildLowStockAlert("MFG-PEKAN", AlertStatus.OPEN);

        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(user);
        when(alertRepository.findById(101L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponse response = alertService.acknowledgeAlert(
                101L,
                "manufacturer@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(response.getReferenceLabel()).isEqualTo("STK-001");
        verify(alertRepository).save(alert);
    }

    @Test
    void lowStockEvaluationCreatesHighSeverityAlertWhenQuantityIsZero() {
        StockAlertRule rule = buildStockRule("MFG-PEKAN");

        when(alertRepository
                .findByStockAlertRuleIdAndAlertTypeAndStatusInOrderByCreatedAtDesc(
                        null,
                        AlertType.LOW_STOCK,
                        List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
                )).thenReturn(List.of());
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertService.synchronizeLowStockAlert(rule, 0);

        verify(alertRepository).save(org.mockito.ArgumentMatchers.argThat(alert ->
                alert.getAlertType() == AlertType.LOW_STOCK
                        && alert.getSeverity() == AlertSeverity.HIGH
                        && alert.getShipment() == null
                        && alert.getStockAlertRule() == rule
        ));
    }

    @Test
    void lowStockEvaluationResolvesExistingAlertAfterRecovery() {
        StockAlertRule rule = buildStockRule("MFG-PEKAN");
        Alert existingAlert = buildLowStockAlert("MFG-PEKAN", AlertStatus.ACKNOWLEDGED);

        when(alertRepository
                .findByStockAlertRuleIdAndAlertTypeAndStatusInOrderByCreatedAtDesc(
                        null,
                        AlertType.LOW_STOCK,
                        List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
                )).thenReturn(List.of(existingAlert));

        alertService.synchronizeLowStockAlert(rule, 5);

        assertThat(existingAlert.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(existingAlert.getResolvedAt()).isNotNull();
        verify(alertRepository).saveAll(List.of(existingAlert));
    }

    @Test
    void orderUpdateReplacesPreviousOpenNotification() {
        Shipment shipment = new Shipment();
        shipment.setShipmentCode("SHP-001");
        Alert previousAlert = buildAlert("SHP-001", "MFG-PEKAN", "DLR-KL");
        previousAlert.setAlertType(AlertType.ORDER_UPDATE);

        when(shipmentService.findShipmentEntityById(1L)).thenReturn(shipment);
        when(alertRepository.findByShipmentIdAndAlertTypeAndStatusInOrderByCreatedAtDesc(
                1L,
                AlertType.ORDER_UPDATE,
                List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
        )).thenReturn(List.of(previousAlert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponse response = alertService.createOrderUpdateAlert(
                1L,
                ShipmentStatus.CREATED,
                ShipmentStatus.READY_FOR_DISPATCH,
                ShipmentEventSourceType.USER_ACTION
        );

        assertThat(previousAlert.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(response.getAlertType()).isEqualTo(AlertType.ORDER_UPDATE);
        assertThat(response.getMessage()).contains("Ready For Dispatch");
        verify(alertRepository).saveAll(List.of(previousAlert));
    }

    private AppUser buildUser(UserRole role, String partyProfileKey) {
        AppUser user = new AppUser();
        user.setRole(role);
        user.setPartyProfileKey(partyProfileKey);
        return user;
    }

    private Alert buildAlert(
            String shipmentCode,
            String manufacturerKey,
            String dealershipKey
    ) {
        Shipment shipment = new Shipment();
        shipment.setShipmentCode(shipmentCode);
        shipment.setManufacturerKey(manufacturerKey);
        shipment.setDealershipKey(dealershipKey);

        Alert alert = new Alert();
        alert.setShipment(shipment);
        alert.setAlertType(AlertType.DELAY);
        alert.setSeverity(AlertSeverity.HIGH);
        alert.setStatus(AlertStatus.OPEN);
        alert.setTitle("Delay detected");
        alert.setMessage("Shipment speed is below the expected threshold.");
        return alert;
    }

    private Alert buildLowStockAlert(String partyKey, AlertStatus status) {
        Alert alert = new Alert();
        alert.setStockAlertRule(buildStockRule(partyKey));
        alert.setAlertType(AlertType.LOW_STOCK);
        alert.setSeverity(AlertSeverity.MEDIUM);
        alert.setStatus(status);
        alert.setTitle("Low stock");
        alert.setMessage("Stock is below the configured minimum.");
        return alert;
    }

    private StockAlertRule buildStockRule(String partyKey) {
        ShipmentPartyProfile partyProfile = new ShipmentPartyProfile();
        partyProfile.setPartyKey(partyKey);
        partyProfile.setPartyRole(UserRole.MANUFACTURER);
        partyProfile.setCompanyName("Pekan Manufacturing");

        VehicleModel vehicleModel = new VehicleModel();
        vehicleModel.setModelCode("MODEL-001");
        vehicleModel.setDisplayName("C-Class");

        StockAlertRule rule = new StockAlertRule();
        rule.setRuleKey("STK-001");
        rule.setPartyProfile(partyProfile);
        rule.setVehicleModel(vehicleModel);
        rule.setLogisticsStage(LogisticsStage.STOCK_YARD);
        rule.setMinimumQuantity(3);
        rule.setActive(true);
        return rule;
    }
}
