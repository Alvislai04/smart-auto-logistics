package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.ShipmentEventResponse;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShipmentEventService {

    private final ShipmentEventRepository shipmentEventRepository;
    private final ShipmentService shipmentService;
    private final BlockchainRecordService blockchainRecordService;

    public ShipmentEventService(
            ShipmentEventRepository shipmentEventRepository,
            ShipmentService shipmentService,
            BlockchainRecordService blockchainRecordService
    ) {
        this.shipmentEventRepository = shipmentEventRepository;
        this.shipmentService = shipmentService;
        this.blockchainRecordService = blockchainRecordService;
    }

    @Transactional
    public ShipmentEventResponse createEvent(
            Long shipmentId,
            ShipmentEventType eventType,
            String description,
            String previousStatus,
            String newStatus
    ) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);

        ShipmentEvent shipmentEvent = new ShipmentEvent();
        shipmentEvent.setShipment(shipment);
        shipmentEvent.setEventType(eventType);
        shipmentEvent.setDescription(description);
        shipmentEvent.setPreviousStatus(previousStatus);
        shipmentEvent.setNewStatus(newStatus);

        ShipmentEvent savedEvent = shipmentEventRepository.save(shipmentEvent);
        return mapToResponse(savedEvent);
    }

    @Transactional
    public ShipmentEventResponse createEventForShipment(
            Shipment shipment,
            ShipmentEventType eventType,
            String description,
            String previousStatus,
            String newStatus
    ) {
        ShipmentEvent shipmentEvent = new ShipmentEvent();
        shipmentEvent.setShipment(shipment);
        shipmentEvent.setEventType(eventType);
        shipmentEvent.setDescription(description);
        shipmentEvent.setPreviousStatus(previousStatus);
        shipmentEvent.setNewStatus(newStatus);

        ShipmentEvent savedEvent = shipmentEventRepository.save(shipmentEvent);

        recordToBlockchainIfRequired(savedEvent);

        return mapToResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<ShipmentEventResponse> getEventsByShipmentId(Long shipmentId) {
        shipmentService.findShipmentEntityById(shipmentId);

        return shipmentEventRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void recordToBlockchainIfRequired(ShipmentEvent shipmentEvent) {
        boolean shouldRecord = switch (shipmentEvent.getEventType()) {
            case SHIPMENT_CREATED,
                STATUS_CHANGED,
                DELAY_DETECTED,
                IDLE_DETECTED,
                ALERT_CREATED,
                DELIVERY_CONFIRMED -> true;

            case TRACKING_UPDATED -> false;
        };

        if (shouldRecord) {
            blockchainRecordService.recordShipmentEventAutomatically(shipmentEvent);
        }
    }

    private ShipmentEventResponse mapToResponse(ShipmentEvent shipmentEvent) {
        return new ShipmentEventResponse(
                shipmentEvent.getId(),
                shipmentEvent.getShipment().getId(),
                shipmentEvent.getShipment().getShipmentCode(),
                shipmentEvent.getEventType(),
                shipmentEvent.getDescription(),
                shipmentEvent.getPreviousStatus(),
                shipmentEvent.getNewStatus(),
                shipmentEvent.getCreatedAt()
        );
    }
}