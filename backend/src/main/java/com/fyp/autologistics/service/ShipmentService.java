package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.CreateShipmentRequest;
import com.fyp.autologistics.dto.ShipmentResponse;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.entity.ShipmentEvent;

import java.util.List;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final BlockchainRecordService blockchainRecordService;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository shipmentEventRepository,
            BlockchainRecordService blockchainRecordService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.blockchainRecordService = blockchainRecordService;
    }

    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        Shipment shipment = new Shipment();
        shipment.setShipmentCode(request.getShipmentCode());
        shipment.setVehicleVin(request.getVehicleVin());
        shipment.setVehicleModel(request.getVehicleModel());
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());
        shipment.setDealershipName(request.getDealershipName());
        shipment.setEstimatedArrival(request.getEstimatedArrival());
        shipment.setStatus(ShipmentStatus.CREATED);

        Shipment savedShipment = shipmentRepository.save(shipment);

        createShipmentEvent(
                savedShipment,
                ShipmentEventType.SHIPMENT_CREATED,
                "Shipment was created.",
                null,
                savedShipment.getStatus().name()
        );

        return mapToResponse(savedShipment);
    }

    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = findShipmentEntityById(id);
        return mapToResponse(shipment);
    }

    public ShipmentResponse updateShipmentStatus(Long id, ShipmentStatus status) {
        Shipment shipment = findShipmentEntityById(id);
        ShipmentStatus previousStatus = shipment.getStatus();

        validateStatusTransition(previousStatus, status);

        shipment.setStatus(status);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        createShipmentEvent(
                updatedShipment,
                ShipmentEventType.STATUS_CHANGED,
                "Shipment status changed from " + previousStatus + " to " + status + ".",
                previousStatus.name(),
                status.name()
        );

        return mapToResponse(updatedShipment);
    }

    public void deleteShipment(Long id) {
        Shipment shipment = findShipmentEntityById(id);
        shipmentRepository.delete(shipment);
    }

    public Shipment findShipmentEntityById(Long id) {
        return shipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
    }

    public Shipment saveShipmentEntity(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    private void validateStatusTransition(
            ShipmentStatus currentStatus,
            ShipmentStatus nextStatus
    ) {
        if (currentStatus == nextStatus) {
            return;
        }

        if (currentStatus == ShipmentStatus.CANCELLED) {
            throw new BadRequestException("Cancelled shipments cannot be updated.");
        }

        if (currentStatus == ShipmentStatus.DELIVERED) {
            throw new BadRequestException("Delivered shipments cannot be updated.");
    }

        boolean isValidTransition = switch (currentStatus) {
            case CREATED -> nextStatus == ShipmentStatus.READY_FOR_DISPATCH
                    || nextStatus == ShipmentStatus.CANCELLED;

            case READY_FOR_DISPATCH -> nextStatus == ShipmentStatus.PICKED_UP
                    || nextStatus == ShipmentStatus.CANCELLED;

            case PICKED_UP -> nextStatus == ShipmentStatus.IN_TRANSIT
                    || nextStatus == ShipmentStatus.CANCELLED;

            case IN_TRANSIT -> nextStatus == ShipmentStatus.ARRIVED
                    || nextStatus == ShipmentStatus.DELAYED
                    || nextStatus == ShipmentStatus.CANCELLED;

            case DELAYED -> nextStatus == ShipmentStatus.IN_TRANSIT
                    || nextStatus == ShipmentStatus.ARRIVED
                    || nextStatus == ShipmentStatus.CANCELLED;

            case ARRIVED -> nextStatus == ShipmentStatus.DELIVERED
                    || nextStatus == ShipmentStatus.CANCELLED;

            case DELIVERED, CANCELLED -> false;
        };

        if (!isValidTransition) {
            throw new BadRequestException(
                "Invalid shipment status transition from "
                        + currentStatus
                        + " to "
                        + nextStatus
            );
        }
    }

    private void createShipmentEvent(
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

    private ShipmentResponse mapToResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getShipmentCode(),
                shipment.getVehicleVin(),
                shipment.getVehicleModel(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getDealershipName(),
                shipment.getStatus(),
                shipment.getEstimatedArrival(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}