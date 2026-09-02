package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.ConfirmDeliveryReceiptRequest;
import com.fyp.autologistics.dto.CreateShipmentRequest;
import com.fyp.autologistics.dto.ShipmentResponse;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.event.ShipmentStatusChangedEvent;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShipmentServiceTests {

    private final ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
    private final ShipmentEventRepository shipmentEventRepository =
            mock(ShipmentEventRepository.class);
    private final VehicleModelRepository vehicleModelRepository = mock(VehicleModelRepository.class);
    private final ShipmentPartyProfileRepository shipmentPartyProfileRepository =
            mock(ShipmentPartyProfileRepository.class);
    private final BlockchainRecordService blockchainRecordService =
            mock(BlockchainRecordService.class);
    private final AuthenticatedUserService authenticatedUserService =
            mock(AuthenticatedUserService.class);
    private final ApplicationEventPublisher applicationEventPublisher =
            mock(ApplicationEventPublisher.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final Clock applicationClock = Clock.fixed(
            Instant.parse("2026-08-26T04:00:00Z"),
            ZoneId.of("Asia/Kuala_Lumpur")
    );

    private final ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            shipmentEventRepository,
            vehicleModelRepository,
            shipmentPartyProfileRepository,
            blockchainRecordService,
            authenticatedUserService,
            applicationEventPublisher,
            entityManager,
            applicationClock
    );

    @Test
    void createShipmentRejectsPastEstimatedArrival() {
        CreateShipmentRequest request = new CreateShipmentRequest();
        request.setEstimatedArrival(LocalDateTime.now(applicationClock).minusMinutes(1));
        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(buildActor(
                        "Manufacturer Operator",
                        "manufacturer@example.com",
                        UserRole.MANUFACTURER,
                        "MFG-001"
                ));

        assertThatThrownBy(() -> shipmentService.createShipment(
                request,
                "manufacturer@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Estimated arrival must be in the future.");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void manufacturerCannotCreateShipmentForAnotherCompanyProfile() {
        CreateShipmentRequest request = new CreateShipmentRequest();
        request.setEstimatedArrival(LocalDateTime.now(applicationClock).plusDays(1));
        request.setManufacturerKey("MFG-OTHER");
        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(buildActor(
                        "Manufacturer Operator",
                        "manufacturer@example.com",
                        UserRole.MANUFACTURER,
                        "MFG-001"
                ));

        assertThatThrownBy(() -> shipmentService.createShipment(
                request,
                "manufacturer@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Manufacturers can only create shipments for their own company profile.");

        verify(vehicleModelRepository, never()).findByModelCodeAndActiveTrue(any());
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void unchangedManualStatusDoesNotCreateDuplicateHistoryOrAlertEvent() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT, LogisticsStage.IN_TRANSIT);
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("logistics@example.com"))
                .thenReturn(buildActor(
                        "Logistics Operator",
                        "logistics@example.com",
                        UserRole.LOGISTICS_PROVIDER,
                        null
                ));

        ShipmentResponse response = shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.IN_TRANSIT,
                "logistics@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentEventRepository, never()).save(any(ShipmentEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void validManualTransitionCreatesAttributedHistoryAndPublishesAfterChange() {
        Shipment shipment = buildShipment(ShipmentStatus.CREATED, LogisticsStage.MANUFACTURING);
        AppUser actor = buildActor(
                "Logistics Operator",
                "logistics@example.com",
                UserRole.LOGISTICS_PROVIDER,
                null
        );
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("logistics@example.com"))
                .thenReturn(actor);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponse response = shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.IN_TRANSIT,
                "logistics@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(response.getCurrentStage()).isEqualTo(LogisticsStage.IN_TRANSIT);

        ArgumentCaptor<ShipmentEvent> historyCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
        verify(shipmentEventRepository).save(historyCaptor.capture());
        ShipmentEvent history = historyCaptor.getValue();
        assertThat(history.getEventType()).isEqualTo(ShipmentEventType.STATUS_CHANGED);
        assertThat(history.getActorEmail()).isEqualTo("logistics@example.com");
        assertThat(history.getActorRole()).isEqualTo(UserRole.LOGISTICS_PROVIDER);
        assertThat(history.getSourceType()).isEqualTo(ShipmentEventSourceType.USER_ACTION);
        verify(blockchainRecordService).recordShipmentEventAutomatically(history);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isEqualTo(new ShipmentStatusChangedEvent(
                42L,
                ShipmentStatus.CREATED,
                ShipmentStatus.IN_TRANSIT,
                ShipmentEventSourceType.USER_ACTION
        ));
    }

    @Test
    void invalidManualTransitionIsRejectedWithoutPersistence() {
        Shipment shipment = buildShipment(ShipmentStatus.CREATED, LogisticsStage.MANUFACTURING);
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("admin@example.com"))
                .thenReturn(buildActor());

        assertThatThrownBy(() -> shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.DELIVERED,
                "admin@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid shipment status transition");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentEventRepository, never()).save(any(ShipmentEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void deliveredStatusRequiresAssignedDealershipReceiptConfirmation() {
        Shipment shipment = buildShipment(
                ShipmentStatus.ARRIVED,
                LogisticsStage.ARRIVED_AT_DEALERSHIP
        );
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("logistics@example.com"))
                .thenReturn(buildActor(
                        "Logistics Operator",
                        "logistics@example.com",
                        UserRole.LOGISTICS_PROVIDER,
                        "LSP-001"
                ));

        assertThatThrownBy(() -> shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.DELIVERED,
                "logistics@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("assigned dealership confirms receipt");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.ARRIVED);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void logisticsProviderCannotCancelAfterShipmentArrives() {
        Shipment shipment = buildShipment(
                ShipmentStatus.ARRIVED,
                LogisticsStage.ARRIVED_AT_DEALERSHIP
        );
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("logistics@example.com"))
                .thenReturn(buildActor(
                        "Logistics Operator",
                        "logistics@example.com",
                        UserRole.LOGISTICS_PROVIDER,
                        "LSP-001"
                ));

        assertThatThrownBy(() -> shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.CANCELLED,
                "logistics@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid shipment status transition");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.ARRIVED);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void manufacturerCannotStartTransit() {
        Shipment shipment = buildShipment(ShipmentStatus.CREATED, LogisticsStage.MANUFACTURING);
        AppUser manufacturer = buildActor(
                "Manufacturer Operator",
                "manufacturer@example.com",
                UserRole.MANUFACTURER,
                "MFG-001"
        );

        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(manufacturer);
        assertThatThrownBy(() -> shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.IN_TRANSIT,
                "manufacturer@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("logistics providers start transit, confirm arrival, or cancel shipments");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void logisticsProviderCanStartTransit() {
        Shipment shipment = buildShipment(ShipmentStatus.CREATED, LogisticsStage.MANUFACTURING);
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("logistics@example.com"))
                .thenReturn(buildActor(
                        "Logistics Operator",
                        "logistics@example.com",
                        UserRole.LOGISTICS_PROVIDER,
                        "LSP-001"
                ));
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponse response = shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.IN_TRANSIT,
                "logistics@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(response.getCurrentStage()).isEqualTo(LogisticsStage.IN_TRANSIT);
    }

    @Test
    void logisticsProviderCanConfirmArrival() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT, LogisticsStage.IN_TRANSIT);
        AppUser actor = buildActor(
                "Logistics Operator",
                "logistics@example.com",
                UserRole.LOGISTICS_PROVIDER,
                "LSP-001"
        );
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("logistics@example.com"))
                .thenReturn(actor);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponse response = shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.ARRIVED,
                "logistics@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.ARRIVED);
        assertThat(response.getCurrentStage())
                .isEqualTo(LogisticsStage.ARRIVED_AT_DEALERSHIP);

        ArgumentCaptor<ShipmentEvent> historyCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
        verify(shipmentEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getActorEmail())
                .isEqualTo("logistics@example.com");
        assertThat(historyCaptor.getValue().getSourceType())
                .isEqualTo(ShipmentEventSourceType.USER_ACTION);
    }

    @Test
    void manufacturerCannotCancelShipment() {
        Shipment shipment = buildShipment(ShipmentStatus.CREATED, LogisticsStage.MANUFACTURING);
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(buildActor(
                        "Manufacturer Operator",
                        "manufacturer@example.com",
                        UserRole.MANUFACTURER,
                        "MFG-001"
                ));

        assertThatThrownBy(() -> shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.CANCELLED,
                "manufacturer@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("logistics providers start transit, confirm arrival, or cancel shipments");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void assignedDealershipConfirmationCompletesDeliveryAndUsesAuthenticatedActor() {
        Shipment shipment = buildShipment(
                ShipmentStatus.ARRIVED,
                LogisticsStage.ARRIVED_AT_DEALERSHIP
        );
        shipment.setDealershipKey("DLR-001");
        AppUser dealership = buildActor(
                "Dealership Receiver",
                "dealer@example.com",
                UserRole.DEALERSHIP,
                "DLR-001"
        );
        ConfirmDeliveryReceiptRequest request = new ConfirmDeliveryReceiptRequest();
        request.setConfirmedBy("Untrusted Client Value");

        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("dealer@example.com"))
                .thenReturn(dealership);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponse response = shipmentService.confirmDeliveryReceipt(
                42L,
                request,
                "dealer@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(response.getCurrentStage()).isEqualTo(LogisticsStage.DELIVERED);
        assertThat(response.getReceiptConfirmedBy()).isEqualTo("Dealership Receiver");
        assertThat(response.getReceiptConfirmedAt()).isNotNull();
    }

    @Test
    void arrivalStageAllowsDealershipReceiptConfirmationEvenIfStatusIsStale() {
        Shipment shipment = buildShipment(
                ShipmentStatus.IN_TRANSIT,
                LogisticsStage.ARRIVED_AT_DEALERSHIP
        );
        shipment.setDealershipKey("DLR-001");
        AppUser dealership = buildActor(
                "Dealership Receiver",
                "dealer@example.com",
                UserRole.DEALERSHIP,
                "DLR-001"
        );
        ConfirmDeliveryReceiptRequest request = new ConfirmDeliveryReceiptRequest();

        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("dealer@example.com"))
                .thenReturn(dealership);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponse response = shipmentService.confirmDeliveryReceipt(
                42L,
                request,
                "dealer@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(response.getCurrentStage()).isEqualTo(LogisticsStage.DELIVERED);
    }

    @Test
    void unassignedDealershipCannotConfirmReceipt() {
        Shipment shipment = buildShipment(
                ShipmentStatus.ARRIVED,
                LogisticsStage.ARRIVED_AT_DEALERSHIP
        );
        shipment.setDealershipKey("DLR-001");
        ConfirmDeliveryReceiptRequest request = new ConfirmDeliveryReceiptRequest();
        request.setConfirmedBy("Other Dealership");

        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("other@example.com"))
                .thenReturn(buildActor(
                        "Other Dealership",
                        "other@example.com",
                        UserRole.DEALERSHIP,
                        "DLR-002"
                ));

        assertThatThrownBy(() -> shipmentService.confirmDeliveryReceipt(
                42L,
                request,
                "other@example.com"
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("dealership assigned to this shipment");

        assertThat(shipment.getReceiptConfirmedAt()).isNull();
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void systemStageOnlyChangeDoesNotCreateOrderUpdate() {
        Shipment shipment = buildShipment(
                ShipmentStatus.READY_FOR_DISPATCH,
                LogisticsStage.STOCK_YARD
        );
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        Shipment updated = shipmentService.updateSystemStatus(
                shipment,
                ShipmentStatus.READY_FOR_DISPATCH,
                LogisticsStage.READY_FOR_DISPATCH,
                ShipmentEventSourceType.SYSTEM_AUTOMATION,
                "Stage synchronized."
        );

        assertThat(updated.getCurrentStage()).isEqualTo(LogisticsStage.READY_FOR_DISPATCH);
        verify(shipmentRepository).save(shipment);
        verify(shipmentEventRepository, never()).save(any(ShipmentEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void systemStatusChangePublishesIoTOrderUpdateSource() {
        Shipment shipment = buildShipment(ShipmentStatus.PICKED_UP, LogisticsStage.STOCK_YARD);
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        shipmentService.updateSystemStatus(
                shipment,
                ShipmentStatus.IN_TRANSIT,
                LogisticsStage.IN_TRANSIT,
                ShipmentEventSourceType.IOT_SIMULATION,
                "Automatic movement detected."
        );

        ArgumentCaptor<ShipmentEvent> historyCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
        verify(shipmentEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getSourceType())
                .isEqualTo(ShipmentEventSourceType.IOT_SIMULATION);

        verify(applicationEventPublisher).publishEvent(new ShipmentStatusChangedEvent(
                42L,
                ShipmentStatus.PICKED_UP,
                ShipmentStatus.IN_TRANSIT,
                ShipmentEventSourceType.IOT_SIMULATION
        ));
    }

    @Test
    void staleSystemUpdateCannotOverwriteArrivedShipment() {
        Shipment staleSimulationShipment = buildShipment(
                ShipmentStatus.IN_TRANSIT,
                LogisticsStage.IN_TRANSIT
        );
        Shipment currentShipment = buildShipment(
                ShipmentStatus.ARRIVED,
                LogisticsStage.ARRIVED_AT_DEALERSHIP
        );
        when(shipmentRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(currentShipment));

        Shipment result = shipmentService.updateSystemStatus(
                staleSimulationShipment,
                ShipmentStatus.DELAYED,
                LogisticsStage.IN_TRANSIT,
                ShipmentEventSourceType.IOT_SIMULATION,
                "A delayed simulation update arrived late."
        );

        assertThat(result.getStatus()).isEqualTo(ShipmentStatus.ARRIVED);
        assertThat(result.getCurrentStage())
                .isEqualTo(LogisticsStage.ARRIVED_AT_DEALERSHIP);
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentEventRepository, never()).save(any(ShipmentEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    private Shipment buildShipment(ShipmentStatus status, LogisticsStage stage) {
        Shipment shipment = new Shipment();
        ReflectionTestUtils.setField(shipment, "id", 42L);
        shipment.setShipmentCode("SHP-TEST-001");
        shipment.setStatus(status);
        shipment.setCurrentStage(stage);
        return shipment;
    }

    private AppUser buildActor() {
        return buildActor(
                "System Administrator",
                "admin@example.com",
                UserRole.ADMIN,
                null
        );
    }

    private AppUser buildActor(
            String fullName,
            String email,
            UserRole role,
            String partyProfileKey
    ) {
        AppUser actor = new AppUser();
        ReflectionTestUtils.setField(actor, "id", 7L);
        actor.setFullName(fullName);
        actor.setEmail(email);
        actor.setRole(role);
        actor.setPartyProfileKey(partyProfileKey);
        return actor;
    }
}
