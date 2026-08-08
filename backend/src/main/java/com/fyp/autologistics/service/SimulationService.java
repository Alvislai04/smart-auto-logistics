package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.CreateTrackingEventRequest;
import com.fyp.autologistics.dto.SimulateTrackingRequest;
import com.fyp.autologistics.dto.TrackingEventResponse;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.ShipmentStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulationService {

    private final ShipmentService shipmentService;
    private final TrackingEventService trackingEventService;

    public SimulationService(
            ShipmentService shipmentService,
            TrackingEventService trackingEventService
    ) {
        this.shipmentService = shipmentService;
        this.trackingEventService = trackingEventService;
    }

    public TrackingEventResponse simulateNextTrackingPoint(
            Long shipmentId,
            SimulateTrackingRequest request
    ) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);

        markShipmentAsInTransitIfRequired(shipment);

        double nextLatitude = generateNextCoordinate(request.getCurrentLatitude());
        double nextLongitude = generateNextCoordinate(request.getCurrentLongitude());
        double speed = request.getSpeed() != null ? request.getSpeed() : generateRandomSpeed();

        ConditionStatus conditionStatus = determineConditionStatus(speed);

        updateShipmentStatusBasedOnCondition(shipmentId, conditionStatus);

        TrackingEventResponse trackingEventResponse = createTrackingEvent(
                shipmentId,
                nextLatitude,
                nextLongitude,
                speed,
                conditionStatus
        );

        return trackingEventResponse;
    }

    private void markShipmentAsInTransitIfRequired(Shipment shipment) {
        if (shipment.getStatus() == ShipmentStatus.CREATED
                || shipment.getStatus() == ShipmentStatus.READY_FOR_DISPATCH
                || shipment.getStatus() == ShipmentStatus.PICKED_UP) {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            shipmentService.saveShipmentEntity(shipment);
        }
    }

    private TrackingEventResponse createTrackingEvent(
            Long shipmentId,
            double latitude,
            double longitude,
            double speed,
            ConditionStatus conditionStatus
    ) {
        CreateTrackingEventRequest trackingRequest = new CreateTrackingEventRequest();
        trackingRequest.setLatitude(latitude);
        trackingRequest.setLongitude(longitude);
        trackingRequest.setSpeed(speed);
        trackingRequest.setConditionStatus(conditionStatus);

        return trackingEventService.createTrackingEvent(shipmentId, trackingRequest);
    }

    private double generateNextCoordinate(Double currentCoordinate) {
        double movementOffset = ThreadLocalRandom.current().nextDouble(0.001, 0.006);
        return currentCoordinate + movementOffset;
    }

    private double generateRandomSpeed() {
        return ThreadLocalRandom.current().nextDouble(20.0, 90.0);
    }

    private ConditionStatus determineConditionStatus(double speed) {
        if (speed <= 1.0) {
            return ConditionStatus.IDLE;
        }

        if (speed < 15.0) {
            return ConditionStatus.DELAYED;
        }

        return ConditionStatus.NORMAL;
    }

    private void updateShipmentStatusBasedOnCondition(
            Long shipmentId,
            ConditionStatus conditionStatus
    ) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);

        if (conditionStatus == ConditionStatus.DELAYED
                || conditionStatus == ConditionStatus.IDLE
                || conditionStatus == ConditionStatus.ROUTE_DEVIATION) {
            shipment.setStatus(ShipmentStatus.DELAYED);
            shipmentService.saveShipmentEntity(shipment);
            return;
        }

        if (conditionStatus == ConditionStatus.NORMAL
                && shipment.getStatus() == ShipmentStatus.DELAYED) {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            shipmentService.saveShipmentEntity(shipment);
        }
    }
}
