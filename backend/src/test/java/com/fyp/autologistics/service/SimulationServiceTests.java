package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.CreateTrackingEventRequest;
import com.fyp.autologistics.dto.TrackingEventResponse;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.SimulationProfile;
import com.fyp.autologistics.entity.SimulationSession;
import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.SimulationSessionStatus;
import com.fyp.autologistics.event.ShipmentCreatedEvent;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.SimulationProfileRepository;
import com.fyp.autologistics.repository.SimulationSessionRepository;
import com.fyp.autologistics.repository.TrackingEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimulationServiceTests {

    private static final Long SHIPMENT_ID = 42L;

    private final ShipmentService shipmentService = mock(ShipmentService.class);
    private final TrackingEventService trackingEventService = mock(TrackingEventService.class);
    private final TrackingEventRepository trackingEventRepository =
            mock(TrackingEventRepository.class);
    private final ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
    private final SimulationProfileRepository simulationProfileRepository =
            mock(SimulationProfileRepository.class);
    private final SimulationSessionRepository simulationSessionRepository =
            mock(SimulationSessionRepository.class);

    private final SimulationService simulationService = new SimulationService(
            shipmentService,
            trackingEventService,
            trackingEventRepository,
            shipmentRepository,
            simulationProfileRepository,
            simulationSessionRepository,
            true,
            15,
            500
    );

    @Test
    void completedSessionPreventsAnotherSystemSessionFromBeingCreated() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);
        SimulationSession completedSession = buildSession(
                shipment,
                SimulationSessionStatus.COMPLETED
        );

        when(shipmentService.findShipmentEntityById(SHIPMENT_ID)).thenReturn(shipment);
        when(simulationProfileRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                .thenReturn(List.of(buildNormalProfile()));
        when(simulationSessionRepository.findFirstByShipmentIdOrderByUpdatedAtDesc(SHIPMENT_ID))
                .thenReturn(Optional.of(completedSession));

        simulationService.createSessionAfterShipmentCreated(new ShipmentCreatedEvent(SHIPMENT_ID));

        verify(simulationSessionRepository, never()).save(any(SimulationSession.class));
    }

    @Test
    void normalTelemetryDoesNotClearAnAlertEngineDelay() {
        LocalDateTime now = LocalDateTime.now();
        Shipment delayedShipment = buildShipment(ShipmentStatus.DELAYED);
        SimulationSession activeSession = buildSession(
                delayedShipment,
                SimulationSessionStatus.ACTIVE
        );
        activeSession.setNextRunAt(now.minusSeconds(1));

        when(simulationProfileRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                .thenReturn(List.of(buildNormalProfile()));
        when(shipmentRepository.findShipmentsWithoutSimulationSession(any()))
                .thenReturn(List.of());
        when(simulationSessionRepository
                .findByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
                        eq(SimulationSessionStatus.ACTIVE),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(activeSession));
        when(trackingEventService.createSystemTrackingEvent(
                eq(SHIPMENT_ID),
                any(CreateTrackingEventRequest.class),
                eq(ShipmentEventSourceType.IOT_SIMULATION)
        )).thenReturn(new TrackingEventResponse(
                1L,
                SHIPMENT_ID,
                0.001,
                0.001,
                60.0,
                ConditionStatus.NORMAL,
                now
        ));

        simulationService.processDueSimulationSessions();

        verify(shipmentService, never()).updateSystemStatus(
                any(),
                eq(ShipmentStatus.IN_TRANSIT),
                any(),
                any(),
                any()
        );
        verify(simulationSessionRepository).save(activeSession);
    }

    private Shipment buildShipment(ShipmentStatus status) {
        Shipment shipment = new Shipment();
        ReflectionTestUtils.setField(shipment, "id", SHIPMENT_ID);
        shipment.setShipmentCode("SHP-TEST-042");
        shipment.setStatus(status);
        shipment.setOriginLatitude(0.0);
        shipment.setOriginLongitude(0.0);
        shipment.setDestinationLatitude(10.0);
        shipment.setDestinationLongitude(10.0);
        return shipment;
    }

    private SimulationSession buildSession(
            Shipment shipment,
            SimulationSessionStatus status
    ) {
        SimulationSession session = new SimulationSession();
        session.setShipment(shipment);
        session.setStatus(status);
        session.setIntervalSeconds(15);
        session.setSpeed(60.0);
        session.setConditionStatus(ConditionStatus.NORMAL);
        session.setCurrentLatitude(0.0);
        session.setCurrentLongitude(0.0);
        session.setGeneratedEvents(0);
        session.setMaxEvents(500);
        session.setSystemManaged(true);
        return session;
    }

    private SimulationProfile buildNormalProfile() {
        SimulationProfile profile = new SimulationProfile();
        profile.setProfileKey("TEST_NORMAL");
        profile.setDisplayName("Test normal movement");
        profile.setConditionStatus(ConditionStatus.NORMAL);
        profile.setMinSpeed(60.0);
        profile.setMaxSpeed(60.0);
        profile.setWeight(1);
        profile.setRouteDeviationEnabled(false);
        profile.setRouteDeviationLatitudeOffset(0.0);
        profile.setActive(true);
        profile.setSortOrder(1);
        return profile;
    }
}
