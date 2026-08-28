package com.fyp.autologistics.service;

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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
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
            applicationClock
    );

    @Test
    void unchangedManualStatusDoesNotCreateDuplicateHistoryOrAlertEvent() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT, LogisticsStage.IN_TRANSIT);
        when(shipmentRepository.findById(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("admin@example.com"))
                .thenReturn(buildActor());

        ShipmentResponse response = shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.IN_TRANSIT,
                "admin@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentEventRepository, never()).save(any(ShipmentEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void validManualTransitionCreatesAttributedHistoryAndPublishesAfterChange() {
        Shipment shipment = buildShipment(ShipmentStatus.CREATED, LogisticsStage.STOCK_YARD);
        AppUser actor = buildActor();
        when(shipmentRepository.findById(42L)).thenReturn(Optional.of(shipment));
        when(authenticatedUserService.getAuthenticatedUser("admin@example.com"))
                .thenReturn(actor);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponse response = shipmentService.updateShipmentStatus(
                42L,
                ShipmentStatus.READY_FOR_DISPATCH,
                "admin@example.com"
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.READY_FOR_DISPATCH);
        assertThat(response.getCurrentStage()).isEqualTo(LogisticsStage.READY_FOR_DISPATCH);

        ArgumentCaptor<ShipmentEvent> historyCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
        verify(shipmentEventRepository).save(historyCaptor.capture());
        ShipmentEvent history = historyCaptor.getValue();
        assertThat(history.getEventType()).isEqualTo(ShipmentEventType.STATUS_CHANGED);
        assertThat(history.getActorEmail()).isEqualTo("admin@example.com");
        assertThat(history.getActorRole()).isEqualTo(UserRole.ADMIN);
        assertThat(history.getSourceType()).isEqualTo(ShipmentEventSourceType.USER_ACTION);
        verify(blockchainRecordService).recordShipmentEventAutomatically(history);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isEqualTo(new ShipmentStatusChangedEvent(
                42L,
                ShipmentStatus.CREATED,
                ShipmentStatus.READY_FOR_DISPATCH,
                ShipmentEventSourceType.USER_ACTION
        ));
    }

    @Test
    void invalidManualTransitionIsRejectedWithoutPersistence() {
        Shipment shipment = buildShipment(ShipmentStatus.CREATED, LogisticsStage.MANUFACTURING);
        when(shipmentRepository.findById(42L)).thenReturn(Optional.of(shipment));
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
    void systemStageOnlyChangeDoesNotCreateOrderUpdate() {
        Shipment shipment = buildShipment(
                ShipmentStatus.READY_FOR_DISPATCH,
                LogisticsStage.STOCK_YARD
        );
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

    private Shipment buildShipment(ShipmentStatus status, LogisticsStage stage) {
        Shipment shipment = new Shipment();
        ReflectionTestUtils.setField(shipment, "id", 42L);
        shipment.setShipmentCode("SHP-TEST-001");
        shipment.setStatus(status);
        shipment.setCurrentStage(stage);
        return shipment;
    }

    private AppUser buildActor() {
        AppUser actor = new AppUser();
        ReflectionTestUtils.setField(actor, "id", 7L);
        actor.setFullName("System Administrator");
        actor.setEmail("admin@example.com");
        actor.setRole(UserRole.ADMIN);
        return actor;
    }
}
