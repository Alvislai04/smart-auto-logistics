package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.CreateTrackingEventRequest;
import com.fyp.autologistics.dto.SimulateTrackingRequest;
import com.fyp.autologistics.dto.SimulationConditionOptionResponse;
import com.fyp.autologistics.dto.SimulationOptionsResponse;
import com.fyp.autologistics.dto.SimulationSessionResponse;
import com.fyp.autologistics.dto.StartSimulationSessionRequest;
import com.fyp.autologistics.dto.TrackingEventResponse;
import com.fyp.autologistics.entity.SimulationSession;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.TrackingEvent;
import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.SimulationSessionStatus;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.SimulationSessionRepository;
import com.fyp.autologistics.repository.TrackingEventRepository;
import com.fyp.autologistics.util.GeoDistanceUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulationService {

    private static final int MIN_INTERVAL_SECONDS = 5;
    private static final int DEFAULT_INTERVAL_SECONDS = 30;
    private static final int MAX_INTERVAL_SECONDS = 3600;
    private static final double MIN_SPEED_KMH = 0.0;
    private static final double DEFAULT_SPEED_KMH = 60.0;
    private static final double MAX_SPEED_KMH = 160.0;
    private static final int MAX_GENERATED_EVENTS = 500;
    private static final double ARRIVAL_THRESHOLD_KM = 0.25;
    private static final double ROUTE_DEVIATION_LATITUDE_OFFSET = 0.12;
    private static final List<SimulationSessionStatus> OPEN_SESSION_STATUSES = List.of(
            SimulationSessionStatus.ACTIVE,
            SimulationSessionStatus.PAUSED
    );

    private final ShipmentService shipmentService;
    private final TrackingEventService trackingEventService;
    private final TrackingEventRepository trackingEventRepository;
    private final SimulationSessionRepository simulationSessionRepository;
    private final boolean schedulerEnabled;

    public SimulationService(
            ShipmentService shipmentService,
            TrackingEventService trackingEventService,
            TrackingEventRepository trackingEventRepository,
            SimulationSessionRepository simulationSessionRepository,
            @Value("${app.simulation.scheduler-enabled:true}") boolean schedulerEnabled
    ) {
        this.shipmentService = shipmentService;
        this.trackingEventService = trackingEventService;
        this.trackingEventRepository = trackingEventRepository;
        this.simulationSessionRepository = simulationSessionRepository;
        this.schedulerEnabled = schedulerEnabled;
    }

    @Transactional(readOnly = true)
    public SimulationOptionsResponse getSimulationOptions() {
        return new SimulationOptionsResponse(
                Arrays.stream(ConditionStatus.values())
                        .map(conditionStatus -> new SimulationConditionOptionResponse(
                                conditionStatus.name(),
                                formatEnumLabel(conditionStatus.name())
                        ))
                        .toList(),
                DEFAULT_INTERVAL_SECONDS,
                MIN_INTERVAL_SECONDS,
                MAX_INTERVAL_SECONDS,
                DEFAULT_SPEED_KMH,
                MIN_SPEED_KMH,
                MAX_SPEED_KMH,
                MAX_GENERATED_EVENTS
        );
    }

    @Transactional
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

    @Transactional
    public SimulationSessionResponse startSimulationSession(
            Long shipmentId,
            StartSimulationSessionRequest request,
            String createdBy
    ) {
        Shipment shipment = shipmentService.findShipmentEntityById(shipmentId);
        validateShipmentCanBeSimulated(shipment);
        ensureNoOpenSession(shipmentId);

        double[] initialPosition = resolveInitialPosition(shipment);

        SimulationSession simulationSession = new SimulationSession();
        simulationSession.setShipment(shipment);
        simulationSession.setStatus(SimulationSessionStatus.ACTIVE);
        simulationSession.setIntervalSeconds(resolveIntervalSeconds(request.getIntervalSeconds()));
        simulationSession.setSpeed(resolveSpeed(request.getSpeed()));
        simulationSession.setConditionStatus(request.getConditionStatus());
        simulationSession.setCurrentLatitude(initialPosition[0]);
        simulationSession.setCurrentLongitude(initialPosition[1]);
        simulationSession.setGeneratedEvents(0);
        simulationSession.setMaxEvents(resolveMaxEvents(request.getMaxEvents()));
        simulationSession.setCreatedBy(createdBy);
        simulationSession.setNextRunAt(LocalDateTime.now());

        SimulationSession savedSession = simulationSessionRepository.save(simulationSession);
        processSession(savedSession, LocalDateTime.now());

        return mapToResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public Optional<SimulationSessionResponse> getShipmentSimulationSession(Long shipmentId) {
        shipmentService.findShipmentEntityById(shipmentId);

        return simulationSessionRepository.findFirstByShipmentIdOrderByUpdatedAtDesc(shipmentId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<SimulationSessionResponse> getSimulationSessions(
            SimulationSessionStatus status
    ) {
        List<SimulationSession> sessions = status == null
                ? simulationSessionRepository.findAllByOrderByUpdatedAtDesc()
                : simulationSessionRepository.findByStatusOrderByUpdatedAtDesc(status);

        return sessions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public SimulationSessionResponse pauseSimulationSession(Long sessionId) {
        SimulationSession session = findSimulationSession(sessionId);

        if (session.getStatus() != SimulationSessionStatus.ACTIVE) {
            throw new BadRequestException("Only active simulation sessions can be paused.");
        }

        session.setStatus(SimulationSessionStatus.PAUSED);
        session.setNextRunAt(null);

        return mapToResponse(simulationSessionRepository.save(session));
    }

    @Transactional
    public SimulationSessionResponse resumeSimulationSession(Long sessionId) {
        SimulationSession session = findSimulationSession(sessionId);

        if (session.getStatus() != SimulationSessionStatus.PAUSED) {
            throw new BadRequestException("Only paused simulation sessions can be resumed.");
        }

        validateShipmentCanBeSimulated(session.getShipment());
        session.setStatus(SimulationSessionStatus.ACTIVE);
        session.setNextRunAt(LocalDateTime.now());

        return mapToResponse(simulationSessionRepository.save(session));
    }

    @Transactional
    public SimulationSessionResponse stopSimulationSession(Long sessionId) {
        SimulationSession session = findSimulationSession(sessionId);

        if (session.getStatus() == SimulationSessionStatus.STOPPED
                || session.getStatus() == SimulationSessionStatus.COMPLETED) {
            return mapToResponse(session);
        }

        session.setStatus(SimulationSessionStatus.STOPPED);
        session.setNextRunAt(null);

        return mapToResponse(simulationSessionRepository.save(session));
    }

    @Scheduled(fixedDelayString = "${app.simulation.scheduler-delay-ms:5000}")
    @Transactional
    public void processDueSimulationSessions() {
        if (!schedulerEnabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        simulationSessionRepository
                .findByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
                        SimulationSessionStatus.ACTIVE,
                        now
                )
                .forEach(session -> processSession(session, now));
    }

    private void markShipmentAsInTransitIfRequired(Shipment shipment) {
        if (shipment.getStatus() == ShipmentStatus.CREATED
                || shipment.getStatus() == ShipmentStatus.READY_FOR_DISPATCH
                || shipment.getStatus() == ShipmentStatus.PICKED_UP) {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            shipment.setCurrentStage(LogisticsStage.IN_TRANSIT);
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

    private void validateShipmentCanBeSimulated(Shipment shipment) {
        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.CANCELLED
                || shipment.getStatus() == ShipmentStatus.ARRIVED) {
            throw new BadRequestException(
                    "Simulation can only run for shipments that have not arrived, "
                            + "been delivered, or been cancelled."
            );
        }

        if (shipment.getStatus() == ShipmentStatus.CREATED
                || shipment.getStatus() == ShipmentStatus.READY_FOR_DISPATCH) {
            throw new BadRequestException(
                    "Simulation can only start after the shipment is picked up "
                            + "or already in transit."
            );
        }

        if (shipment.getOriginLatitude() == null
                || shipment.getOriginLongitude() == null
                || shipment.getDestinationLatitude() == null
                || shipment.getDestinationLongitude() == null) {
            throw new BadRequestException(
                    "Shipment must have complete origin and destination coordinates "
                            + "before simulation can start."
            );
        }
    }

    private void ensureNoOpenSession(Long shipmentId) {
        simulationSessionRepository
                .findFirstByShipmentIdAndStatusInOrderByCreatedAtDesc(
                        shipmentId,
                        OPEN_SESSION_STATUSES
                )
                .ifPresent(session -> {
                    throw new BadRequestException(
                            "This shipment already has an active or paused simulation session."
                    );
                });
    }

    private SimulationSession findSimulationSession(Long sessionId) {
        return simulationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Simulation session not found with id: " + sessionId
                ));
    }

    private double[] resolveInitialPosition(Shipment shipment) {
        Optional<TrackingEvent> latestTrackingEvent =
                trackingEventRepository.findFirstByShipmentIdOrderByRecordedAtDesc(
                        shipment.getId()
                );

        return latestTrackingEvent
                .filter(this::hasTrackingCoordinates)
                .map(trackingEvent -> new double[] {
                        trackingEvent.getLatitude(),
                        trackingEvent.getLongitude()
                })
                .orElseGet(() -> new double[] {
                        shipment.getOriginLatitude(),
                        shipment.getOriginLongitude()
                });
    }

    private void processSession(SimulationSession session, LocalDateTime now) {
        if (session.getStatus() != SimulationSessionStatus.ACTIVE) {
            return;
        }

        Shipment shipment = session.getShipment();

        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            completeSession(session, now);
            return;
        }

        markShipmentAsInTransitIfRequired(shipment);

        ConditionStatus conditionStatus = resolveConditionStatusForSession(session);
        double[] nextPosition = calculateNextPosition(session, shipment, conditionStatus);

        TrackingEventResponse trackingEvent = createTrackingEvent(
                shipment.getId(),
                nextPosition[0],
                nextPosition[1],
                session.getSpeed(),
                conditionStatus
        );

        session.setCurrentLatitude(trackingEvent.getLatitude());
        session.setCurrentLongitude(trackingEvent.getLongitude());
        session.setLastRunAt(now);
        session.setGeneratedEvents(session.getGeneratedEvents() + 1);

        if (hasReachedDestination(session, shipment)
                || hasReachedMaxEvents(session)) {
            if (hasReachedDestination(session, shipment)) {
                markShipmentArrived(shipment);
            }

            completeSession(session, now);
            return;
        }

        session.setNextRunAt(now.plusSeconds(session.getIntervalSeconds()));
        simulationSessionRepository.save(session);
    }

    private ConditionStatus resolveConditionStatusForSession(SimulationSession session) {
        if (session.getConditionStatus() != null) {
            return session.getConditionStatus();
        }

        return determineConditionStatus(session.getSpeed());
    }

    private double[] calculateNextPosition(
            SimulationSession session,
            Shipment shipment,
            ConditionStatus conditionStatus
    ) {
        double currentLatitude = session.getCurrentLatitude();
        double currentLongitude = session.getCurrentLongitude();
        double destinationLatitude = shipment.getDestinationLatitude();
        double destinationLongitude = shipment.getDestinationLongitude();
        double distanceRemainingKm = GeoDistanceUtils.haversineDistanceKm(
                currentLatitude,
                currentLongitude,
                destinationLatitude,
                destinationLongitude
        );

        if (distanceRemainingKm <= ARRIVAL_THRESHOLD_KM) {
            return new double[] { destinationLatitude, destinationLongitude };
        }

        double travelDistanceKm = resolveTravelDistanceKm(session, conditionStatus);

        if (travelDistanceKm <= 0.0) {
            return new double[] { currentLatitude, currentLongitude };
        }

        double progressRatio = Math.min(1.0, travelDistanceKm / distanceRemainingKm);
        double nextLatitude =
                currentLatitude + ((destinationLatitude - currentLatitude) * progressRatio);
        double nextLongitude =
                currentLongitude + ((destinationLongitude - currentLongitude) * progressRatio);

        if (conditionStatus == ConditionStatus.ROUTE_DEVIATION) {
            nextLatitude += ROUTE_DEVIATION_LATITUDE_OFFSET;
        }

        return new double[] { nextLatitude, nextLongitude };
    }

    private double resolveTravelDistanceKm(
            SimulationSession session,
            ConditionStatus conditionStatus
    ) {
        if (conditionStatus == ConditionStatus.IDLE || session.getSpeed() <= 0.0) {
            return 0.0;
        }

        return session.getSpeed() * session.getIntervalSeconds() / 3600.0;
    }

    private boolean hasReachedDestination(SimulationSession session, Shipment shipment) {
        double distanceRemainingKm = GeoDistanceUtils.haversineDistanceKm(
                session.getCurrentLatitude(),
                session.getCurrentLongitude(),
                shipment.getDestinationLatitude(),
                shipment.getDestinationLongitude()
        );

        return distanceRemainingKm <= ARRIVAL_THRESHOLD_KM;
    }

    private boolean hasReachedMaxEvents(SimulationSession session) {
        return session.getMaxEvents() != null
                && session.getGeneratedEvents() >= session.getMaxEvents();
    }

    private void markShipmentArrived(Shipment shipment) {
        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.CANCELLED
                || shipment.getStatus() == ShipmentStatus.ARRIVED) {
            return;
        }

        shipment.setStatus(ShipmentStatus.ARRIVED);
        shipment.setCurrentStage(LogisticsStage.ARRIVED_AT_DEALERSHIP);
        shipmentService.saveShipmentEntity(shipment);
    }

    private void completeSession(SimulationSession session, LocalDateTime now) {
        session.setStatus(SimulationSessionStatus.COMPLETED);
        session.setLastRunAt(now);
        session.setNextRunAt(null);
        simulationSessionRepository.save(session);
    }

    private int resolveIntervalSeconds(Integer intervalSeconds) {
        if (intervalSeconds < MIN_INTERVAL_SECONDS || intervalSeconds > MAX_INTERVAL_SECONDS) {
            throw new BadRequestException(
                    "Simulation interval must be between "
                            + MIN_INTERVAL_SECONDS
                            + " and "
                            + MAX_INTERVAL_SECONDS
                            + " seconds."
            );
        }

        return intervalSeconds;
    }

    private double resolveSpeed(Double speed) {
        if (speed < MIN_SPEED_KMH || speed > MAX_SPEED_KMH) {
            throw new BadRequestException(
                    "Simulation speed must be between "
                            + MIN_SPEED_KMH
                            + " and "
                            + MAX_SPEED_KMH
                            + " km/h."
            );
        }

        return speed;
    }

    private Integer resolveMaxEvents(Integer maxEvents) {
        if (maxEvents == null) {
            return null;
        }

        if (maxEvents < 1 || maxEvents > MAX_GENERATED_EVENTS) {
            throw new BadRequestException(
                    "Maximum generated events must be between 1 and "
                            + MAX_GENERATED_EVENTS
                            + "."
            );
        }

        return maxEvents;
    }

    private boolean hasTrackingCoordinates(TrackingEvent trackingEvent) {
        return trackingEvent.getLatitude() != null
                && trackingEvent.getLongitude() != null;
    }

    private SimulationSessionResponse mapToResponse(SimulationSession session) {
        Shipment shipment = session.getShipment();

        return new SimulationSessionResponse(
                session.getId(),
                shipment.getId(),
                shipment.getShipmentCode(),
                shipment.getStatus(),
                session.getStatus(),
                session.getIntervalSeconds(),
                session.getSpeed(),
                session.getConditionStatus(),
                session.getCurrentLatitude(),
                session.getCurrentLongitude(),
                session.getNextRunAt(),
                session.getLastRunAt(),
                session.getGeneratedEvents(),
                session.getMaxEvents(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getDealershipName(),
                session.getCreatedBy(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private String formatEnumLabel(String value) {
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("_"))
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse(value);
    }
}
