package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.ShipmentEventResponse;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
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
    public ShipmentEventResponse createEventForShipment(
            Shipment shipment,
            ShipmentEventType eventType,
            String description,
            String previousStatus,
            String newStatus
    ) {
        return createEventForShipment(
                shipment,
                eventType,
                description,
                previousStatus,
                newStatus,
                null,
                ShipmentEventSourceType.SYSTEM_AUTOMATION
        );
    }

    @Transactional
    public ShipmentEventResponse createEventForShipment(
            Shipment shipment,
            ShipmentEventType eventType,
            String description,
            String previousStatus,
            String newStatus,
            AppUser actor,
            ShipmentEventSourceType sourceType
    ) {
        ShipmentEvent shipmentEvent = new ShipmentEvent();
        shipmentEvent.setShipment(shipment);
        shipmentEvent.setEventType(eventType);
        shipmentEvent.setDescription(description);
        shipmentEvent.setPreviousStatus(previousStatus);
        shipmentEvent.setNewStatus(newStatus);
        applyEventMetadata(shipmentEvent, actor, sourceType);

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
                SHIPMENT_UPDATED,
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

    private void applyEventMetadata(
            ShipmentEvent shipmentEvent,
            AppUser actor,
            ShipmentEventSourceType sourceType
    ) {
        if (actor != null) {
            shipmentEvent.setActorUserId(actor.getId());
            shipmentEvent.setActorName(actor.getFullName());
            shipmentEvent.setActorEmail(actor.getEmail());
            shipmentEvent.setActorRole(actor.getRole());
        }

        shipmentEvent.setSourceType(
                sourceType == null ? ShipmentEventSourceType.SYSTEM_AUTOMATION : sourceType
        );
        shipmentEvent.setEventPayloadVersion(2);
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
                shipmentEvent.getActorUserId(),
                shipmentEvent.getActorName(),
                shipmentEvent.getActorEmail(),
                shipmentEvent.getActorRole(),
                shipmentEvent.getSourceType(),
                shipmentEvent.getEventPayloadVersion(),
                shipmentEvent.getCreatedAt()
        );
    }
}
