package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.CreateTrackingEventRequest;
import com.fyp.autologistics.dto.SimulationSessionResponse;
import com.fyp.autologistics.dto.TrackingEventResponse;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.SimulationProfile;
import com.fyp.autologistics.entity.SimulationSession;
import com.fyp.autologistics.entity.TrackingEvent;
import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.SimulationSessionStatus;
import com.fyp.autologistics.event.ShipmentStatusChangedEvent;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.SimulationProfileRepository;
import com.fyp.autologistics.repository.SimulationSessionRepository;
import com.fyp.autologistics.repository.TrackingEventRepository;
import com.fyp.autologistics.util.GeoDistanceUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulationService {

    private static final String SYSTEM_ACTOR = "SYSTEM";
    private static final int MIN_INTERVAL_SECONDS = 5;
    private static final int MAX_INTERVAL_SECONDS = 3600;
    private static final int MIN_MAX_EVENTS = 1;
    private static final int MAX_MAX_EVENTS = 10_000;
    private static final double ARRIVAL_THRESHOLD_KM = 0.25;
    private static final double MAX_ROUTE_DEVIATION_OFFSET_DEGREES = 0.1;
    private static final double ROUTE_COMPLETION_EVENT_BUFFER = 2.0;
    private static final List<ShipmentStatus> SYSTEM_SIMULATED_SHIPMENT_STATUSES = List.of(
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.DELAYED
    );
    private final ShipmentService shipmentService;
    private final TrackingEventService trackingEventService;
    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentRepository shipmentRepository;
    private final SimulationProfileRepository simulationProfileRepository;
    private final SimulationSessionRepository simulationSessionRepository;
    private final boolean schedulerEnabled;
    private final int defaultIntervalSeconds;
    private final int defaultMaxEvents;

    public SimulationService(
            ShipmentService shipmentService,
            TrackingEventService trackingEventService,
            TrackingEventRepository trackingEventRepository,
            ShipmentRepository shipmentRepository,
            SimulationProfileRepository simulationProfileRepository,
            SimulationSessionRepository simulationSessionRepository,
            @Value("${app.simulation.scheduler-enabled:true}") boolean schedulerEnabled,
            @Value("${app.simulation.default-interval-seconds:15}") int defaultIntervalSeconds,
            @Value("${app.simulation.default-max-events:500}") int defaultMaxEvents
    ) {
        this.shipmentService = shipmentService;
        this.trackingEventService = trackingEventService;
        this.trackingEventRepository = trackingEventRepository;
        this.shipmentRepository = shipmentRepository;
        this.simulationProfileRepository = simulationProfileRepository;
        this.simulationSessionRepository = simulationSessionRepository;
        this.schedulerEnabled = schedulerEnabled;
        this.defaultIntervalSeconds = clamp(
                defaultIntervalSeconds,
                MIN_INTERVAL_SECONDS,
                MAX_INTERVAL_SECONDS
        );
        this.defaultMaxEvents = clamp(defaultMaxEvents, MIN_MAX_EVENTS, MAX_MAX_EVENTS);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createSessionAfterTransitStarted(ShipmentStatusChangedEvent event) {
        if (!schedulerEnabled || event.currentStatus() != ShipmentStatus.IN_TRANSIT) {
            return;
        }

        Shipment shipment = shipmentService.findShipmentEntityById(event.shipmentId());
        List<SimulationProfile> profiles = loadActiveProfiles();

        createSystemSessionIfMissing(shipment, LocalDateTime.now(), profiles);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void completeSessionAfterTransitEnded(ShipmentStatusChangedEvent event) {
        if (!isSimulationTerminalStatus(event.currentStatus())) {
            return;
        }

        simulationSessionRepository
                .findFirstByShipmentIdAndStatusOrderByUpdatedAtDesc(
                        event.shipmentId(),
                        SimulationSessionStatus.ACTIVE
                )
                .ifPresent(session -> completeSession(session, LocalDateTime.now()));
    }

    @Scheduled(fixedDelayString = "${app.simulation.scheduler-delay-ms:5000}")
    @Transactional
    public void processDueSimulationSessions() {
        if (!schedulerEnabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SimulationProfile> profiles = loadActiveProfiles();

        if (profiles.isEmpty()) {
            return;
        }

        provisionMissingSystemSessions(now, profiles);

        simulationSessionRepository
                .findByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
                        SimulationSessionStatus.ACTIVE,
                        now
                )
                .forEach(session -> processSession(session, now, profiles));
    }

    private List<SimulationProfile> loadActiveProfiles() {
        return simulationProfileRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc();
    }

    private void provisionMissingSystemSessions(
            LocalDateTime now,
            List<SimulationProfile> profiles
    ) {
        shipmentRepository
                .findShipmentsWithoutSimulationSession(SYSTEM_SIMULATED_SHIPMENT_STATUSES)
                .forEach(shipment -> createSystemSessionIfMissing(shipment, now, profiles));
    }

    private Optional<SimulationSession> createSystemSessionIfMissing(
            Shipment shipment,
            LocalDateTime now,
            List<SimulationProfile> profiles
    ) {
        if (profiles.isEmpty() || !canRunSystemSimulation(shipment)) {
            return Optional.empty();
        }

        Optional<SimulationSession> existingSession =
                simulationSessionRepository.findFirstByShipmentIdOrderByUpdatedAtDesc(
                        shipment.getId()
                );

        if (existingSession
                .map(SimulationSession::getStatus)
                .filter(SimulationSessionStatus.ACTIVE::equals)
                .isPresent()) {
            return Optional.empty();
        }

        SimulationProfile profile = selectProfile(profiles);
        double speed = generateSpeed(profile);
        double[] initialPosition = resolveInitialPosition(shipment);

        SimulationSession simulationSession = new SimulationSession();
        simulationSession.setShipment(shipment);
        simulationSession.setStatus(SimulationSessionStatus.ACTIVE);
        simulationSession.setIntervalSeconds(defaultIntervalSeconds);
        simulationSession.setSpeed(speed);
        simulationSession.setConditionStatus(profile.getConditionStatus());
        simulationSession.setCurrentLatitude(initialPosition[0]);
        simulationSession.setCurrentLongitude(initialPosition[1]);
        simulationSession.setNextRunAt(now);
        simulationSession.setLastRunAt(null);
        simulationSession.setGeneratedEvents(0);
        simulationSession.setMaxEvents(resolveRouteAwareMaxEvents(shipment, profiles));
        simulationSession.setCreatedBy(SYSTEM_ACTOR);
        simulationSession.setSystemManaged(true);
        simulationSession.setLastProfileKey(profile.getProfileKey());
        simulationSession.setLastProfileName(profile.getDisplayName());

        return Optional.of(simulationSessionRepository.save(simulationSession));
    }

    private void processSession(
            SimulationSession session,
            LocalDateTime now,
            List<SimulationProfile> profiles
    ) {
        if (session.getStatus() != SimulationSessionStatus.ACTIVE
                || !canRunSystemSimulation(session.getShipment())) {
            completeSession(session, now);
            return;
        }

        Shipment shipment = session.getShipment();
        ensureRouteAwareMaxEvents(session, shipment, profiles);

        SimulationProfile profile = selectProfile(profiles);
        ConditionStatus conditionStatus = profile.getConditionStatus();
        double speed = generateSpeed(profile);
        double[] nextPosition = calculateNextPosition(session, shipment, profile, speed);

        TrackingEventResponse trackingEvent = createTrackingEvent(
                shipment.getId(),
                nextPosition[0],
                nextPosition[1],
                speed,
                conditionStatus
        );

        session.setSpeed(speed);
        session.setConditionStatus(conditionStatus);
        session.setLastProfileKey(profile.getProfileKey());
        session.setLastProfileName(profile.getDisplayName());
        session.setCurrentLatitude(trackingEvent.getLatitude());
        session.setCurrentLongitude(trackingEvent.getLongitude());
        session.setLastRunAt(now);
        session.setGeneratedEvents(resolveGeneratedEvents(session) + 1);

        boolean reachedDestination = hasReachedDestination(session, shipment);

        if (reachedDestination) {
            completeSession(session, now);
            return;
        }

        updateShipmentStatusBasedOnCondition(shipment, conditionStatus);

        if (hasReachedMaxEvents(session)) {
            completeSession(session, now);
            return;
        }

        session.setNextRunAt(now.plusSeconds(resolveIntervalSeconds(session)));
        simulationSessionRepository.save(session);
    }

    private boolean canRunSystemSimulation(Shipment shipment) {
        return SYSTEM_SIMULATED_SHIPMENT_STATUSES.contains(shipment.getStatus())
                && hasRouteCoordinates(shipment);
    }

    private boolean isSimulationTerminalStatus(ShipmentStatus status) {
        return status == ShipmentStatus.ARRIVED
                || status == ShipmentStatus.DELIVERED
                || status == ShipmentStatus.CANCELLED;
    }

    private boolean hasRouteCoordinates(Shipment shipment) {
        return shipment.getOriginLatitude() != null
                && shipment.getOriginLongitude() != null
                && shipment.getDestinationLatitude() != null
                && shipment.getDestinationLongitude() != null;
    }

    private void updateShipmentStatusBasedOnCondition(
            Shipment shipment,
            ConditionStatus conditionStatus
    ) {
        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.CANCELLED
                || shipment.getStatus() == ShipmentStatus.ARRIVED) {
            return;
        }

        if (conditionStatus == ConditionStatus.DELAYED
                || conditionStatus == ConditionStatus.IDLE
                || conditionStatus == ConditionStatus.ROUTE_DEVIATION
                || conditionStatus == ConditionStatus.SENSOR_WARNING) {
            shipmentService.updateSystemStatus(
                    shipment,
                    ShipmentStatus.DELAYED,
                    LogisticsStage.IN_TRANSIT,
                    ShipmentEventSourceType.IOT_SIMULATION,
                    "System simulation detected a delayed operating condition."
            );
            return;
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

        return trackingEventService.createSystemTrackingEvent(
                shipmentId,
                trackingRequest,
                ShipmentEventSourceType.IOT_SIMULATION
        );
    }

    private SimulationProfile selectProfile(List<SimulationProfile> profiles) {
        int totalWeight = profiles.stream()
                .mapToInt(this::resolveWeight)
                .sum();

        if (totalWeight <= 0) {
            return profiles.getFirst();
        }

        int ticket = ThreadLocalRandom.current().nextInt(totalWeight);

        for (SimulationProfile profile : profiles) {
            int weight = resolveWeight(profile);

            if (ticket < weight) {
                return profile;
            }

            ticket -= weight;
        }

        return profiles.getLast();
    }

    private double generateSpeed(SimulationProfile profile) {
        double minSpeed = Math.max(0.0, resolveSpeed(profile.getMinSpeed(), 0.0));
        double maxSpeed = Math.max(minSpeed, resolveSpeed(profile.getMaxSpeed(), minSpeed));

        if (maxSpeed == minSpeed) {
            return roundToOneDecimal(minSpeed);
        }

        return roundToOneDecimal(ThreadLocalRandom.current().nextDouble(minSpeed, maxSpeed));
    }

    private int resolveWeight(SimulationProfile profile) {
        return Math.max(profile.getWeight() == null ? 0 : profile.getWeight(), 0);
    }

    private double resolveSpeed(Double value, double fallback) {
        return value == null ? fallback : value;
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

    private double[] calculateNextPosition(
            SimulationSession session,
            Shipment shipment,
            SimulationProfile profile,
            double speed
    ) {
        double originLatitude = shipment.getOriginLatitude();
        double originLongitude = shipment.getOriginLongitude();
        double currentLatitude = session.getCurrentLatitude();
        double currentLongitude = session.getCurrentLongitude();
        double destinationLatitude = shipment.getDestinationLatitude();
        double destinationLongitude = shipment.getDestinationLongitude();

        double routeDistanceKm = GeoDistanceUtils.haversineDistanceKm(
                originLatitude,
                originLongitude,
                destinationLatitude,
                destinationLongitude
        );

        if (routeDistanceKm <= ARRIVAL_THRESHOLD_KM) {
            return new double[] { destinationLatitude, destinationLongitude };
        }

        double currentProgress = GeoDistanceUtils.progressRatioOnSegment(
                currentLatitude,
                currentLongitude,
                originLatitude,
                originLongitude,
                destinationLatitude,
                destinationLongitude
        );

        double travelDistanceKm = resolveTravelDistanceKm(
                speed,
                resolveIntervalSeconds(session),
                profile.getConditionStatus()
        );

        double nextProgress = Math.min(
                1.0,
                currentProgress + (travelDistanceKm / routeDistanceKm)
        );
        double routeLatitude = interpolate(originLatitude, destinationLatitude, nextProgress);
        double routeLongitude = interpolate(originLongitude, destinationLongitude, nextProgress);

        if (nextProgress >= 1.0) {
            return new double[] { destinationLatitude, destinationLongitude };
        }

        return applyBoundedRouteDeviation(
                routeLatitude,
                routeLongitude,
                shipment,
                profile
        );
    }

    private double[] applyBoundedRouteDeviation(
            double routeLatitude,
            double routeLongitude,
            Shipment shipment,
            SimulationProfile profile
    ) {
        double deviationOffset = Math.max(
                -MAX_ROUTE_DEVIATION_OFFSET_DEGREES,
                Math.min(
                        MAX_ROUTE_DEVIATION_OFFSET_DEGREES,
                        resolveSpeed(profile.getRouteDeviationLatitudeOffset(), 0.0)
                )
        );

        if (!Boolean.TRUE.equals(profile.getRouteDeviationEnabled())
                || deviationOffset == 0.0) {
            return new double[] { routeLatitude, routeLongitude };
        }

        double routeLatitudeDelta =
                shipment.getDestinationLatitude() - shipment.getOriginLatitude();
        double routeLongitudeDelta =
                shipment.getDestinationLongitude() - shipment.getOriginLongitude();
        double routeLengthDegrees = Math.hypot(routeLatitudeDelta, routeLongitudeDelta);

        if (routeLengthDegrees == 0.0) {
            return new double[] { routeLatitude, routeLongitude };
        }

        double perpendicularLatitude = -routeLongitudeDelta / routeLengthDegrees;
        double perpendicularLongitude = routeLatitudeDelta / routeLengthDegrees;

        return new double[] {
                routeLatitude + (perpendicularLatitude * deviationOffset),
                routeLongitude + (perpendicularLongitude * deviationOffset)
        };
    }

    private double interpolate(double start, double end, double progress) {
        return start + ((end - start) * progress);
    }

    private double resolveTravelDistanceKm(
            double speed,
            int intervalSeconds,
            ConditionStatus conditionStatus
    ) {
        if (conditionStatus == ConditionStatus.IDLE || speed <= 0.0) {
            return 0.0;
        }

        return speed * intervalSeconds / 3600.0;
    }

    private void ensureRouteAwareMaxEvents(
            SimulationSession session,
            Shipment shipment,
            List<SimulationProfile> profiles
    ) {
        int requiredMaxEvents = resolveRouteAwareMaxEvents(shipment, profiles);

        if (session.getMaxEvents() == null || session.getMaxEvents() < requiredMaxEvents) {
            session.setMaxEvents(requiredMaxEvents);
        }
    }

    private int resolveRouteAwareMaxEvents(
            Shipment shipment,
            List<SimulationProfile> profiles
    ) {
        double weightedAverageSpeed = calculateWeightedAverageSpeed(profiles);

        if (weightedAverageSpeed <= 0.0) {
            return defaultMaxEvents;
        }

        double routeDistanceKm = GeoDistanceUtils.haversineDistanceKm(
                shipment.getOriginLatitude(),
                shipment.getOriginLongitude(),
                shipment.getDestinationLatitude(),
                shipment.getDestinationLongitude()
        );
        double distancePerEventKm =
                weightedAverageSpeed * defaultIntervalSeconds / 3600.0;
        long routeAwareEvents = (long) Math.ceil(
                routeDistanceKm / distancePerEventKm * ROUTE_COMPLETION_EVENT_BUFFER
        );
        long requiredEvents = Math.max(defaultMaxEvents, routeAwareEvents);

        return (int) Math.min(requiredEvents, MAX_MAX_EVENTS);
    }

    private double calculateWeightedAverageSpeed(List<SimulationProfile> profiles) {
        double weightedSpeedTotal = 0.0;
        int totalWeight = 0;

        for (SimulationProfile profile : profiles) {
            int weight = resolveWeight(profile);
            double minSpeed = Math.max(0.0, resolveSpeed(profile.getMinSpeed(), 0.0));
            double maxSpeed = Math.max(minSpeed, resolveSpeed(profile.getMaxSpeed(), minSpeed));

            weightedSpeedTotal += ((minSpeed + maxSpeed) / 2.0) * weight;
            totalWeight += weight;
        }

        return totalWeight == 0 ? 0.0 : weightedSpeedTotal / totalWeight;
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
                && resolveGeneratedEvents(session) >= session.getMaxEvents();
    }

    private int resolveGeneratedEvents(SimulationSession session) {
        return session.getGeneratedEvents() == null ? 0 : session.getGeneratedEvents();
    }

    private int resolveIntervalSeconds(SimulationSession session) {
        if (session.getIntervalSeconds() == null) {
            return defaultIntervalSeconds;
        }

        return clamp(session.getIntervalSeconds(), MIN_INTERVAL_SECONDS, MAX_INTERVAL_SECONDS);
    }

    private void completeSession(SimulationSession session, LocalDateTime now) {
        session.setStatus(SimulationSessionStatus.COMPLETED);
        session.setLastRunAt(now);
        session.setNextRunAt(null);
        simulationSessionRepository.save(session);
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
                session.getSystemManaged(),
                session.getLastProfileKey(),
                session.getLastProfileName(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
