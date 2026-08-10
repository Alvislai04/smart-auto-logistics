package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.SimulationSessionStatus;

import java.time.LocalDateTime;

public class SimulationSessionResponse {

    private Long id;
    private Long shipmentId;
    private String shipmentCode;
    private ShipmentStatus shipmentStatus;
    private SimulationSessionStatus status;
    private Integer intervalSeconds;
    private Double speed;
    private ConditionStatus conditionStatus;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastRunAt;
    private Integer generatedEvents;
    private Integer maxEvents;
    private String origin;
    private String destination;
    private String dealershipName;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SimulationSessionResponse(
            Long id,
            Long shipmentId,
            String shipmentCode,
            ShipmentStatus shipmentStatus,
            SimulationSessionStatus status,
            Integer intervalSeconds,
            Double speed,
            ConditionStatus conditionStatus,
            Double currentLatitude,
            Double currentLongitude,
            LocalDateTime nextRunAt,
            LocalDateTime lastRunAt,
            Integer generatedEvents,
            Integer maxEvents,
            String origin,
            String destination,
            String dealershipName,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.shipmentCode = shipmentCode;
        this.shipmentStatus = shipmentStatus;
        this.status = status;
        this.intervalSeconds = intervalSeconds;
        this.speed = speed;
        this.conditionStatus = conditionStatus;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.nextRunAt = nextRunAt;
        this.lastRunAt = lastRunAt;
        this.generatedEvents = generatedEvents;
        this.maxEvents = maxEvents;
        this.origin = origin;
        this.destination = destination;
        this.dealershipName = dealershipName;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public String getShipmentCode() {
        return shipmentCode;
    }

    public ShipmentStatus getShipmentStatus() {
        return shipmentStatus;
    }

    public SimulationSessionStatus getStatus() {
        return status;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public Double getSpeed() {
        return speed;
    }

    public ConditionStatus getConditionStatus() {
        return conditionStatus;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public LocalDateTime getNextRunAt() {
        return nextRunAt;
    }

    public LocalDateTime getLastRunAt() {
        return lastRunAt;
    }

    public Integer getGeneratedEvents() {
        return generatedEvents;
    }

    public Integer getMaxEvents() {
        return maxEvents;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getDealershipName() {
        return dealershipName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
