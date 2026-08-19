package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.UserRole;

import java.time.LocalDateTime;

public class ShipmentEventResponse {

    private Long id;
    private Long shipmentId;
    private String shipmentCode;
    private ShipmentEventType eventType;
    private String description;
    private String previousStatus;
    private String newStatus;
    private Long actorUserId;
    private String actorName;
    private String actorEmail;
    private UserRole actorRole;
    private ShipmentEventSourceType sourceType;
    private Integer eventPayloadVersion;
    private LocalDateTime createdAt;

    public ShipmentEventResponse(
            Long id,
            Long shipmentId,
            String shipmentCode,
            ShipmentEventType eventType,
            String description,
            String previousStatus,
            String newStatus,
            Long actorUserId,
            String actorName,
            String actorEmail,
            UserRole actorRole,
            ShipmentEventSourceType sourceType,
            Integer eventPayloadVersion,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.shipmentCode = shipmentCode;
        this.eventType = eventType;
        this.description = description;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.actorUserId = actorUserId;
        this.actorName = actorName;
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.sourceType = sourceType;
        this.eventPayloadVersion = eventPayloadVersion;
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

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getActorName() {
        return actorName;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public UserRole getActorRole() {
        return actorRole;
    }

    public ShipmentEventSourceType getSourceType() {
        return sourceType;
    }

    public Integer getEventPayloadVersion() {
        return eventPayloadVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
