package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.ConfirmDeliveryReceiptRequest;
import com.fyp.autologistics.dto.CreateShipmentRequest;
import com.fyp.autologistics.dto.ShipmentPageResponse;
import com.fyp.autologistics.dto.ShipmentResponse;
import com.fyp.autologistics.dto.UpdateShipmentRequest;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.event.ShipmentCreatedEvent;
import com.fyp.autologistics.event.ShipmentStatusChangedEvent;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.enums.WorkInProgressStatus;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import com.fyp.autologistics.specification.ShipmentSpecifications;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ShipmentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final LogisticsStage INITIAL_LOGISTICS_STAGE = LogisticsStage.MANUFACTURING;
    private static final WorkInProgressStatus INITIAL_WIP_STATUS =
            WorkInProgressStatus.NOT_STARTED;
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
    private final AuthenticatedUserService authenticatedUserService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock applicationClock;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository shipmentEventRepository,
            VehicleModelRepository vehicleModelRepository,
            ShipmentPartyProfileRepository shipmentPartyProfileRepository,
            BlockchainRecordService blockchainRecordService,
            AuthenticatedUserService authenticatedUserService,
            ApplicationEventPublisher applicationEventPublisher,
            Clock applicationClock
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.shipmentPartyProfileRepository = shipmentPartyProfileRepository;
        this.blockchainRecordService = blockchainRecordService;
        this.authenticatedUserService = authenticatedUserService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.applicationClock = applicationClock;
    }

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request, String actorEmail) {
        AppUser actor = authenticatedUserService.getAuthenticatedUser(actorEmail);
        LocalDateTime estimatedDeparture = LocalDateTime.now(applicationClock).withNano(0);
        validateEstimatedArrivalIsFuture(request.getEstimatedArrival());
        validateShipmentSchedule(estimatedDeparture, request.getEstimatedArrival());
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
        shipment.setEstimatedDeparture(estimatedDeparture);
        shipment.setCurrentStage(INITIAL_LOGISTICS_STAGE);
        shipment.setStockLocation(normalizeText(request.getStockLocation()));
        shipment.setWorkInProgressStatus(INITIAL_WIP_STATUS);
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
                savedShipment.getStatus().name(),
                actor,
                ShipmentEventSourceType.USER_ACTION
        );

        applicationEventPublisher.publishEvent(new ShipmentCreatedEvent(savedShipment.getId()));

        return mapToResponse(savedShipment);
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

    @Transactional
    public ShipmentResponse updateShipment(
            Long id,
            UpdateShipmentRequest request,
            String actorEmail
    ) {
        Shipment shipment = findShipmentEntityById(id);
        AppUser actor = authenticatedUserService.getAuthenticatedUser(actorEmail);

        validateShipmentEditAllowed(shipment, actor, request.getManufacturerKey());
        validateEstimatedArrivalIsFuture(request.getEstimatedArrival());
        validateShipmentSchedule(shipment.getEstimatedDeparture(), request.getEstimatedArrival());

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

        List<String> changedFields = new ArrayList<>();

        String vehicleVin = normalizeRequiredText(request.getVehicleVin(), "Vehicle VIN");
        String stockLocation = normalizeRequiredText(request.getStockLocation(), "Stock location");
        String responsiblePerson = normalizeRequiredText(
                request.getResponsiblePerson(),
                "Responsible person"
        );
        String responsibleContact = normalizeRequiredText(
                request.getResponsibleContact(),
                "Responsible contact"
        );
        String customerName = normalizeRequiredText(request.getCustomerName(), "Customer name");
        String customerContact = normalizeRequiredText(
                request.getCustomerContact(),
                "Customer contact"
        );
        String customerAddress = normalizeRequiredText(
                request.getCustomerAddress(),
                "Customer address"
        );
        String shipmentDocumentReference = normalizeText(
                request.getShipmentDocumentReference()
        );
        String origin = resolveOperatingLocation(manufacturer, "Manufacturer origin");
        String destination = resolveOperatingLocation(dealership, "Dealership destination");
        Double originLatitude = resolveLatitude(manufacturer, "Manufacturer origin");
        Double originLongitude = resolveLongitude(manufacturer, "Manufacturer origin");
        Double destinationLatitude = resolveLatitude(dealership, "Dealership destination");
        Double destinationLongitude = resolveLongitude(dealership, "Dealership destination");
        String dealershipName = resolvePartyName(dealership);

        trackChange(changedFields, "vehicle VIN", shipment.getVehicleVin(), vehicleVin);
        trackChange(changedFields, "vehicle model", shipment.getVehicleModelCode(),
                vehicleModel.getModelCode());
        trackChange(changedFields, "manufacturer origin", shipment.getManufacturerKey(),
                manufacturer.getPartyKey());
        trackChange(changedFields, "origin location", shipment.getOrigin(), origin);
        trackChange(changedFields, "origin latitude", shipment.getOriginLatitude(),
                originLatitude);
        trackChange(changedFields, "origin longitude", shipment.getOriginLongitude(),
                originLongitude);
        trackChange(changedFields, "dealership destination", shipment.getDealershipKey(),
                dealership.getPartyKey());
        trackChange(changedFields, "destination location", shipment.getDestination(),
                destination);
        trackChange(changedFields, "destination latitude", shipment.getDestinationLatitude(),
                destinationLatitude);
        trackChange(changedFields, "destination longitude", shipment.getDestinationLongitude(),
                destinationLongitude);
        trackChange(changedFields, "dealership name", shipment.getDealershipName(),
                dealershipName);
        trackChange(changedFields, "estimated arrival", shipment.getEstimatedArrival(),
                request.getEstimatedArrival());
        trackChange(changedFields, "logistics stage", shipment.getCurrentStage(),
                request.getCurrentStage());
        trackChange(changedFields, "stock location", shipment.getStockLocation(),
                stockLocation);
        trackChange(changedFields, "WIP status", shipment.getWorkInProgressStatus(),
                request.getWorkInProgressStatus());
        trackChange(changedFields, "responsible person", shipment.getResponsiblePerson(),
                responsiblePerson);
        trackChange(changedFields, "responsible contact", shipment.getResponsibleContact(),
                responsibleContact);
        trackChange(changedFields, "customer name", shipment.getCustomerName(), customerName);
        trackChange(changedFields, "customer contact", shipment.getCustomerContact(),
                customerContact);
        trackChange(changedFields, "customer address", shipment.getCustomerAddress(),
                customerAddress);
        trackChange(changedFields, "shipment document reference",
                shipment.getShipmentDocumentReference(), shipmentDocumentReference);

        if (changedFields.isEmpty()) {
            return mapToResponse(shipment);
        }

        shipment.setVehicleVin(vehicleVin);
        shipment.setVehicleModelCode(vehicleModel.getModelCode());
        shipment.setVehicleModel(vehicleModel.getDisplayName());
        shipment.setManufacturerKey(manufacturer.getPartyKey());
        shipment.setDealershipKey(dealership.getPartyKey());
        shipment.setOrigin(origin);
        shipment.setDestination(destination);
        shipment.setOriginLatitude(originLatitude);
        shipment.setOriginLongitude(originLongitude);
        shipment.setDestinationLatitude(destinationLatitude);
        shipment.setDestinationLongitude(destinationLongitude);
        shipment.setDealershipName(dealershipName);
        shipment.setEstimatedArrival(request.getEstimatedArrival());
        shipment.setCurrentStage(request.getCurrentStage());
        shipment.setStockLocation(stockLocation);
        shipment.setWorkInProgressStatus(request.getWorkInProgressStatus());
        shipment.setResponsiblePerson(responsiblePerson);
        shipment.setResponsibleContact(responsibleContact);
        shipment.setCustomerName(customerName);
        shipment.setCustomerContact(customerContact);
        shipment.setCustomerAddress(customerAddress);
        shipment.setShipmentDocumentReference(shipmentDocumentReference);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        createShipmentEvent(
                updatedShipment,
                ShipmentEventType.SHIPMENT_UPDATED,
                "Shipment details updated by "
                        + resolveActorLabel(actor)
                        + ". Changed fields: "
                        + String.join(", ", changedFields)
                        + ".",
                null,
                null,
                actor,
                ShipmentEventSourceType.USER_ACTION
        );

        return mapToResponse(updatedShipment);
    }

    @Transactional
    public ShipmentResponse updateShipmentStatus(
            Long id,
            ShipmentStatus status,
            String actorEmail
    ) {
        Shipment shipment = findShipmentEntityById(id);
        AppUser actor = authenticatedUserService.getAuthenticatedUser(actorEmail);
        ShipmentStatus previousStatus = shipment.getStatus();

        if (previousStatus == status) {
            return mapToResponse(shipment);
        }

        validateStatusTransition(previousStatus, status);
        validateManualStatusUpdateAllowed(actor, status);

        shipment.setStatus(status);
        shipment.setCurrentStage(resolveStageForStatus(status, shipment.getCurrentStage()));

        Shipment updatedShipment = shipmentRepository.save(shipment);

        createShipmentEvent(
                updatedShipment,
                ShipmentEventType.STATUS_CHANGED,
                "Shipment status changed from " + previousStatus + " to " + status + ".",
                previousStatus.name(),
                status.name(),
                actor,
                ShipmentEventSourceType.USER_ACTION
        );

        publishStatusChangedEvent(
                updatedShipment,
                previousStatus,
                ShipmentEventSourceType.USER_ACTION
        );

        return mapToResponse(updatedShipment);
    }

    @Transactional
    public ShipmentResponse confirmDeliveryReceipt(
            Long id,
            ConfirmDeliveryReceiptRequest request,
            String actorEmail
    ) {
        Shipment shipment = findShipmentEntityById(id);
        AppUser actor = authenticatedUserService.getAuthenticatedUser(actorEmail);

        validateReceiptConfirmationAllowed(shipment, actor);

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
        String confirmedBy = resolveActorLabel(actor);

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
                updatedShipment.getStatus().name(),
                actor,
                ShipmentEventSourceType.USER_ACTION
        );

        if (previousStatus != updatedShipment.getStatus()) {
            publishStatusChangedEvent(
                    updatedShipment,
                    previousStatus,
                    ShipmentEventSourceType.USER_ACTION
            );
        }

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

    public Shipment updateSystemStatus(
            Shipment shipment,
            ShipmentStatus status,
            LogisticsStage stage,
            ShipmentEventSourceType sourceType,
            String description
    ) {
        ShipmentStatus previousStatus = shipment.getStatus();

        if (previousStatus == status && shipment.getCurrentStage() == stage) {
            return shipment;
        }

        shipment.setStatus(status);
        shipment.setCurrentStage(stage);
        Shipment updatedShipment = shipmentRepository.save(shipment);

        if (previousStatus != status) {
            createShipmentEvent(
                    updatedShipment,
                    ShipmentEventType.STATUS_CHANGED,
                    description,
                    previousStatus == null ? null : previousStatus.name(),
                    status.name(),
                    null,
                    sourceType
            );
            publishStatusChangedEvent(updatedShipment, previousStatus, sourceType);
        }

        return updatedShipment;
    }

    private void publishStatusChangedEvent(
            Shipment shipment,
            ShipmentStatus previousStatus,
            ShipmentEventSourceType sourceType
    ) {
        applicationEventPublisher.publishEvent(new ShipmentStatusChangedEvent(
                shipment.getId(),
                previousStatus,
                shipment.getStatus(),
                sourceType
        ));
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
            case CREATED -> nextStatus == ShipmentStatus.IN_TRANSIT
                    || nextStatus == ShipmentStatus.CANCELLED;

            case READY_FOR_DISPATCH -> nextStatus == ShipmentStatus.PICKED_UP
                    || nextStatus == ShipmentStatus.IN_TRANSIT
                    || nextStatus == ShipmentStatus.CANCELLED;

            case PICKED_UP -> nextStatus == ShipmentStatus.IN_TRANSIT
                    || nextStatus == ShipmentStatus.CANCELLED;

            case IN_TRANSIT -> nextStatus == ShipmentStatus.ARRIVED
                    || nextStatus == ShipmentStatus.DELAYED
                    || nextStatus == ShipmentStatus.CANCELLED;

            case DELAYED -> nextStatus == ShipmentStatus.IN_TRANSIT
                    || nextStatus == ShipmentStatus.ARRIVED
                    || nextStatus == ShipmentStatus.CANCELLED;

            case ARRIVED -> nextStatus == ShipmentStatus.DELIVERED;

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

    private void validateManualStatusUpdateAllowed(AppUser actor, ShipmentStatus nextStatus) {
        if (nextStatus == ShipmentStatus.DELIVERED) {
            throw new BadRequestException(
                    "A shipment becomes delivered only when its assigned dealership confirms receipt."
            );
        }

        boolean actionAllowed = switch (actor.getRole()) {
            case LOGISTICS_PROVIDER -> nextStatus == ShipmentStatus.IN_TRANSIT
                    || nextStatus == ShipmentStatus.ARRIVED
                    || nextStatus == ShipmentStatus.CANCELLED;
            case ADMIN, MANUFACTURER, DEALERSHIP -> false;
        };

        if (!actionAllowed) {
            throw new BadRequestException(resolveStatusPermissionMessage(actor.getRole()));
        }
    }

    private String resolveStatusPermissionMessage(UserRole role) {
        return switch (role) {
            case MANUFACTURER -> "Manufacturers create shipment details; logistics providers start transit, confirm arrival, or cancel shipments.";
            case LOGISTICS_PROVIDER -> "Logistics providers can only start transit, confirm arrival, or cancel shipments.";
            case ADMIN -> "Administrators cannot change shipment workflow status.";
            case DEALERSHIP -> "Dealerships complete delivery by confirming receipt.";
        };
    }

    private void validateReceiptConfirmationAllowed(Shipment shipment, AppUser actor) {
        if (actor.getRole() != UserRole.DEALERSHIP) {
            throw new BadRequestException(
                    "Only the assigned dealership can confirm delivery receipt."
            );
        }

        String actorPartyKey = normalizeText(actor.getPartyProfileKey());
        String shipmentDealershipKey = normalizeText(shipment.getDealershipKey());

        if (actorPartyKey == null) {
            throw new BadRequestException(
                    "Your dealership account is not linked to a shipment party profile."
            );
        }

        if (!actorPartyKey.equals(shipmentDealershipKey)) {
            throw new BadRequestException(
                    "Only the dealership assigned to this shipment can confirm its receipt."
            );
        }
    }

    private void validateShipmentEditAllowed(
            Shipment shipment,
            AppUser actor,
            String requestedManufacturerKey
    ) {
        if (shipment.getStatus() != ShipmentStatus.CREATED
                && shipment.getStatus() != ShipmentStatus.READY_FOR_DISPATCH) {
            throw new BadRequestException(
                    "Shipment details can only be edited before pickup or in-transit tracking begins."
            );
        }

        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }

        if (actor.getRole() != UserRole.MANUFACTURER) {
            throw new BadRequestException("Only manufacturers can edit shipment details.");
        }

        String actorPartyKey = normalizeText(actor.getPartyProfileKey());
        String normalizedRequestedManufacturerKey = normalizeText(requestedManufacturerKey);

        if (actorPartyKey == null) {
            throw new BadRequestException(
                    "Your manufacturer account is not linked to a shipment party profile."
            );
        }

        if (!actorPartyKey.equals(shipment.getManufacturerKey())
                || !actorPartyKey.equals(normalizedRequestedManufacturerKey)) {
            throw new BadRequestException(
                    "Manufacturers can only edit shipments linked to their own company profile."
            );
        }
    }

    private void validateShipmentSchedule(
            LocalDateTime estimatedDeparture,
            LocalDateTime estimatedArrival
    ) {
        if (estimatedDeparture == null || estimatedArrival == null) {
            throw new BadRequestException("Estimated departure and arrival are required.");
        }

        if (!estimatedArrival.isAfter(estimatedDeparture)) {
            throw new BadRequestException("Estimated arrival must be after estimated departure.");
        }
    }

    private void validateEstimatedArrivalIsFuture(LocalDateTime estimatedArrival) {
        if (estimatedArrival == null) {
            throw new BadRequestException("Estimated arrival is required.");
        }

        LocalDateTime now = LocalDateTime.now(applicationClock).withNano(0);
        if (!estimatedArrival.isAfter(now)) {
            throw new BadRequestException("Estimated arrival must be in the future.");
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

    private String normalizeRequiredText(String value, String fieldName) {
        String normalizedValue = normalizeText(value);

        if (normalizedValue == null) {
            throw new BadRequestException(fieldName + " is required.");
        }

        return normalizedValue;
    }

    private void trackChange(
            List<String> changedFields,
            String fieldName,
            Object previousValue,
            Object nextValue
    ) {
        if (!Objects.equals(previousValue, nextValue)) {
            changedFields.add(fieldName);
        }
    }

    private String resolveActorLabel(AppUser actor) {
        if (StringUtils.hasText(actor.getFullName())) {
            return actor.getFullName().trim();
        }

        return actor.getEmail();
    }

    private void createShipmentEvent(
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
