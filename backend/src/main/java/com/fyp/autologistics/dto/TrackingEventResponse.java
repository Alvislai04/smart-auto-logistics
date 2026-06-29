package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ConditionStatus;

import java.time.LocalDateTime;

public class TrackingEventResponse {

    private Long id;
    private Long shipmentId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private ConditionStatus conditionStatus;
    private LocalDateTime recordedAt;

    public TrackingEventResponse(
            Long id,
            Long shipmentId,
            Double latitude,
            Double longitude,
            Double speed,
            ConditionStatus conditionStatus,
            LocalDateTime recordedAt
    ) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.conditionStatus = conditionStatus;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public ConditionStatus getConditionStatus() {
        return conditionStatus;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
}
