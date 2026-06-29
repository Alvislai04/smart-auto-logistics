package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.CreateTrackingEventRequest;
import com.fyp.autologistics.dto.TrackingEventResponse;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.TrackingEvent;
import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.repository.TrackingEventRepository;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingEventService {

    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentService shipmentService;
    private final ShipmentEventService shipmentEventService;

    public TrackingEventService(
            TrackingEventRepository trackingEventRepository,
            ShipmentService shipmentService,
            ShipmentEventService shipmentEventService
    ) {
        this.trackingEventRepository = trackingEventRepository;
        this.shipmentService = shipmentService;
        this.shipmentEventService = shipmentEventService;
    }

    public TrackingEventResponse createTrackingEvent(Long shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);

        TrackingEvent trackingEvent = new TrackingEvent();
        trackingEvent.setShipment(shipment);
        trackingEvent.setLatitude(request.getLatitude());
        trackingEvent.setLongitude(request.getLongitude());
        trackingEvent.setSpeed(request.getSpeed());
        trackingEvent.setConditionStatus(
                request.getConditionStatus() != null
                        ? request.getConditionStatus()
                        : ConditionStatus.NORMAL
        );

        TrackingEvent savedTrackingEvent = trackingEventRepository.save(trackingEvent);

        shipmentEventService.createEventForShipment(
                shipment,
                ShipmentEventType.TRACKING_UPDATED,
                "Tracking updated at latitude "
                        + savedTrackingEvent.getLatitude()
                        + ", longitude "
                        + savedTrackingEvent.getLongitude()
                        + ", speed "
                        + savedTrackingEvent.getSpeed()
                        + " km/h.",
                null,
                null
        );

        return mapToResponse(savedTrackingEvent);
    }

    public List<TrackingEventResponse> getTrackingEventsByShipmentId(Long shipmentId) {
        shipmentService.findShipmentEntityById(shipmentId);

        return trackingEventRepository.findByShipmentIdOrderByRecordedAtAsc(shipmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TrackingEventResponse mapToResponse(TrackingEvent trackingEvent) {
        return new TrackingEventResponse(
                trackingEvent.getId(),
                trackingEvent.getShipment().getId(),
                trackingEvent.getLatitude(),
                trackingEvent.getLongitude(),
                trackingEvent.getSpeed(),
                trackingEvent.getConditionStatus(),
                trackingEvent.getRecordedAt()
        );
    }

    public TrackingEventResponse getLatestTrackingEventByShipmentId(Long shipmentId) {
        shipmentService.findShipmentEntityById(shipmentId);

        TrackingEvent latestTrackingEvent = trackingEventRepository
                .findFirstByShipmentIdOrderByRecordedAtDesc(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No tracking event found for shipment id: " + shipmentId
                ));

        return mapToResponse(latestTrackingEvent);
    }
}