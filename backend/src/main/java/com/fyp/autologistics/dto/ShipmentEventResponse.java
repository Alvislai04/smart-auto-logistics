package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ShipmentEventType;

import java.time.LocalDateTime;

public class ShipmentEventResponse {

    private Long id;
    private Long shipmentId;
    private String shipmentCode;
    private ShipmentEventType eventType;
    private String description;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime createdAt;

    public ShipmentEventResponse(
            Long id,
            Long shipmentId,
            String shipmentCode,
            ShipmentEventType eventType,
            String description,
            String previousStatus,
            String newStatus,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.shipmentCode = shipmentCode;
        this.eventType = eventType;
        this.description = description;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.createdAt = createdAt;
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

    public ShipmentEventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}