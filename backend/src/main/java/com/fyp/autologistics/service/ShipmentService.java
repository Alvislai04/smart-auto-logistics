package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.ConfirmDeliveryReceiptRequest;
import com.fyp.autologistics.dto.CreateShipmentRequest;
import com.fyp.autologistics.dto.ShipmentPageResponse;
import com.fyp.autologistics.dto.ShipmentResponse;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import com.fyp.autologistics.specification.ShipmentSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class ShipmentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final DateTimeFormatter SHIPMENT_CODE_DATE_FORMATTER =
            DateTimeFormatter.BASIC_ISO_DATE;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "shipmentCode",
            "vehicleVin",
            "vehicleModel",
            "origin",
            "destination",
            "dealershipName",
            "status",
            "estimatedDeparture",
            "estimatedArrival",
            "currentStage",
            "responsiblePerson",
            "stockLocation",
            "createdAt",
            "updatedAt"
    );

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final ShipmentPartyProfileRepository shipmentPartyProfileRepository;
    private final BlockchainRecordService blockchainRecordService;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository shipmentEventRepository,
            VehicleModelRepository vehicleModelRepository,
            ShipmentPartyProfileRepository shipmentPartyProfileRepository,
            BlockchainRecordService blockchainRecordService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.shipmentPartyProfileRepository = shipmentPartyProfileRepository;
        this.blockchainRecordService = blockchainRecordService;
    }

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        validateShipmentSchedule(request);
        VehicleModel vehicleModel = findActiveVehicleModel(request.getVehicleModelKey());

        ShipmentPartyProfile manufacturer = findActiveParty(
                request.getManufacturerKey(),
                UserRole.MANUFACTURER,
                "manufacturer"
        );
        ShipmentPartyProfile dealership = findActiveParty(
                request.getDealershipKey(),
                UserRole.DEALERSHIP,
                "dealership"
        );

        Shipment shipment = new Shipment();
        shipment.setVehicleVin(normalizeText(request.getVehicleVin()));
        shipment.setVehicleModelCode(vehicleModel.getModelCode());
        shipment.setVehicleModel(vehicleModel.getDisplayName());
        shipment.setManufacturerKey(manufacturer.getPartyKey());
        shipment.setDealershipKey(dealership.getPartyKey());
        shipment.setOrigin(resolveOperatingLocation(manufacturer, "Manufacturer origin"));
        shipment.setDestination(resolveOperatingLocation(dealership, "Dealership destination"));
        shipment.setOriginLatitude(resolveLatitude(manufacturer, "Manufacturer origin"));
        shipment.setOriginLongitude(resolveLongitude(manufacturer, "Manufacturer origin"));
        shipment.setDestinationLatitude(resolveLatitude(dealership, "Dealership destination"));
        shipment.setDestinationLongitude(resolveLongitude(dealership, "Dealership destination"));
        shipment.setDealershipName(resolvePartyName(dealership));
        shipment.setEstimatedArrival(request.getEstimatedArrival());
        shipment.setEstimatedDeparture(request.getEstimatedDeparture());
        shipment.setCurrentStage(request.getCurrentStage());
        shipment.setStockLocation(normalizeText(request.getStockLocation()));
        shipment.setWorkInProgressStatus(request.getWorkInProgressStatus());
        shipment.setResponsiblePerson(normalizeText(request.getResponsiblePerson()));
        shipment.setResponsibleContact(normalizeText(request.getResponsibleContact()));
        shipment.setCustomerName(normalizeText(request.getCustomerName()));
        shipment.setCustomerContact(normalizeText(request.getCustomerContact()));
        shipment.setCustomerAddress(normalizeText(request.getCustomerAddress()));
        shipment.setShipmentDocumentReference(normalizeText(request.getShipmentDocumentReference()));
        shipment.setStatus(ShipmentStatus.CREATED);

        Shipment savedShipment = shipmentRepository.save(shipment);
        savedShipment.setShipmentCode(generateShipmentCode(savedShipment));
        savedShipment = shipmentRepository.save(savedShipment);

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

    public ShipmentPageResponse searchShipments(
            String searchTerm,
            ShipmentStatus status,
            LogisticsStage stage,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = buildShipmentPageable(page, size, sortBy, sortDirection);

        return ShipmentPageResponse.from(
                shipmentRepository
                        .findAll(
                                ShipmentSpecifications.matchesFilters(searchTerm, status, stage),
                                pageable
                        )
                        .map(this::mapToResponse)
        );
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
        shipment.setCurrentStage(resolveStageForStatus(status, shipment.getCurrentStage()));

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

    public ShipmentResponse confirmDeliveryReceipt(
            Long id,
            ConfirmDeliveryReceiptRequest request
    ) {
        Shipment shipment = findShipmentEntityById(id);

        if (shipment.getReceiptConfirmedAt() != null) {
            throw new BadRequestException("Delivery receipt has already been confirmed.");
        }

        if (shipment.getStatus() != ShipmentStatus.ARRIVED
                && shipment.getStatus() != ShipmentStatus.DELIVERED) {
            throw new BadRequestException(
                    "Delivery receipt can only be confirmed after the shipment arrives."
            );
        }

        ShipmentStatus previousStatus = shipment.getStatus();
        String confirmedBy = normalizeText(request.getConfirmedBy());

        shipment.setReceiptConfirmedAt(LocalDateTime.now());
        shipment.setReceiptConfirmedBy(confirmedBy);
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setCurrentStage(LogisticsStage.DELIVERED);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        createShipmentEvent(
                updatedShipment,
                ShipmentEventType.DELIVERY_CONFIRMED,
                "Delivery receipt confirmed by " + confirmedBy + ".",
                previousStatus.name(),
                updatedShipment.getStatus().name()
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

    private void validateShipmentSchedule(CreateShipmentRequest request) {
        if (!request.getEstimatedArrival().isAfter(request.getEstimatedDeparture())) {
            throw new BadRequestException("Estimated arrival must be after estimated departure.");
        }
    }

    private VehicleModel findActiveVehicleModel(String modelCode) {
        String normalizedModelCode = normalizeText(modelCode);

        if (normalizedModelCode == null) {
            throw new BadRequestException("Vehicle model is required.");
        }

        return vehicleModelRepository.findByModelCodeAndActiveTrue(normalizedModelCode)
                .orElseThrow(() -> new BadRequestException(
                        "Selected vehicle model is not active or does not exist."
                ));
    }

    private ShipmentPartyProfile findActiveParty(
            String partyKey,
            UserRole expectedRole,
            String label
    ) {
        String normalizedPartyKey = normalizeText(partyKey);

        if (normalizedPartyKey == null) {
            throw new BadRequestException("Selected " + label + " is required.");
        }

        return shipmentPartyProfileRepository
                .findByPartyKeyAndPartyRoleAndActiveTrue(normalizedPartyKey, expectedRole)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Selected " + label + " was not found or is inactive."
                ));
    }

    private void validatePartyLocation(ShipmentPartyProfile profile, String label) {
        if (!StringUtils.hasText(profile.getOperatingLocation())
                || profile.getLocationLatitude() == null
                || profile.getLocationLongitude() == null) {
            throw new BadRequestException(
                    "Selected " + label + " does not have a complete registered location profile."
            );
        }
    }

    private String resolvePartyName(ShipmentPartyProfile profile) {
        return profile.getCompanyName().trim();
    }

    private String resolveOperatingLocation(ShipmentPartyProfile profile, String label) {
        validatePartyLocation(profile, label);
        return profile.getOperatingLocation().trim();
    }

    private Double resolveLatitude(ShipmentPartyProfile profile, String label) {
        validatePartyLocation(profile, label);
        return profile.getLocationLatitude();
    }

    private Double resolveLongitude(ShipmentPartyProfile profile, String label) {
        validatePartyLocation(profile, label);
        return profile.getLocationLongitude();
    }

    private String generateShipmentCode(Shipment shipment) {
        String datePart = shipment.getCreatedAt()
                .toLocalDate()
                .format(SHIPMENT_CODE_DATE_FORMATTER);
        String shipmentCode = "SHP-" + datePart + "-" + String.format("%06d", shipment.getId());

        if (shipmentRepository.existsByShipmentCode(shipmentCode)) {
            throw new BadRequestException("Generated shipment code already exists. Please retry.");
        }

        return shipmentCode;
    }

    private Pageable buildShipmentPageable(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative.");
        }

        int resolvedSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        String resolvedSortBy = normalizeSortBy(sortBy);
        Sort.Direction resolvedDirection = resolveSortDirection(sortDirection);

        return PageRequest.of(
                page,
                resolvedSize,
                Sort.by(resolvedDirection, resolvedSortBy)
        );
    }

    private String normalizeSortBy(String sortBy) {
        String resolvedSortBy = normalizeText(sortBy);

        if (resolvedSortBy == null) {
            return "createdAt";
        }

        if (!ALLOWED_SORT_FIELDS.contains(resolvedSortBy)) {
            throw new BadRequestException("Unsupported shipment sort field: " + resolvedSortBy);
        }

        return resolvedSortBy;
    }

    private Sort.Direction resolveSortDirection(String sortDirection) {
        if (sortDirection == null) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Sort direction must be ASC or DESC.");
        }
    }

    private LogisticsStage resolveStageForStatus(
            ShipmentStatus status,
            LogisticsStage fallbackStage
    ) {
        return switch (status) {
            case CREATED -> fallbackStage == null ? LogisticsStage.MANUFACTURING : fallbackStage;
            case READY_FOR_DISPATCH -> LogisticsStage.READY_FOR_DISPATCH;
            case PICKED_UP, IN_TRANSIT, DELAYED -> LogisticsStage.IN_TRANSIT;
            case ARRIVED -> LogisticsStage.ARRIVED_AT_DEALERSHIP;
            case DELIVERED -> LogisticsStage.DELIVERED;
            case CANCELLED -> fallbackStage == null ? LogisticsStage.MANUFACTURING : fallbackStage;
        };
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
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
                shipment.getVehicleModelCode(),
                shipment.getVehicleModel(),
                shipment.getManufacturerKey(),
                shipment.getDealershipKey(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getOriginLatitude(),
                shipment.getOriginLongitude(),
                shipment.getDestinationLatitude(),
                shipment.getDestinationLongitude(),
                shipment.getDealershipName(),
                shipment.getStatus(),
                shipment.getEstimatedArrival(),
                shipment.getEstimatedDeparture(),
                shipment.getCurrentStage(),
                shipment.getStockLocation(),
                shipment.getWorkInProgressStatus(),
                shipment.getResponsiblePerson(),
                shipment.getResponsibleContact(),
                shipment.getCustomerName(),
                shipment.getCustomerContact(),
                shipment.getCustomerAddress(),
                shipment.getShipmentDocumentReference(),
                shipment.getReceiptConfirmedAt(),
                shipment.getReceiptConfirmedBy(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}
