package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.UserRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_events")
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentEventType eventType;

    @Column(nullable = false, length = 1000)
    private String description;

    private String previousStatus;

    private String newStatus;

    private Long actorUserId;

    private String actorName;

    private String actorEmail;

    @Enumerated(EnumType.STRING)
    private UserRole actorRole;

    @Enumerated(EnumType.STRING)
    private ShipmentEventSourceType sourceType = ShipmentEventSourceType.SYSTEM_AUTOMATION;

    private Integer eventPayloadVersion = 2;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    public ShipmentEvent() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.sourceType == null) {
            this.sourceType = ShipmentEventSourceType.SYSTEM_AUTOMATION;
        }

        if (this.eventPayloadVersion == null) {
            this.eventPayloadVersion = 2;
        }
    }

    public Long getId() {
        return id;
    }

    public ShipmentEventType getEventType() {
        return eventType;
    }

    public void setEventType(ShipmentEventType eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public UserRole getActorRole() {
        return actorRole;
    }

    public void setActorRole(UserRole actorRole) {
        this.actorRole = actorRole;
    }

    public ShipmentEventSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ShipmentEventSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getEventPayloadVersion() {
        return eventPayloadVersion;
    }

    public void setEventPayloadVersion(Integer eventPayloadVersion) {
        this.eventPayloadVersion = eventPayloadVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }
}
