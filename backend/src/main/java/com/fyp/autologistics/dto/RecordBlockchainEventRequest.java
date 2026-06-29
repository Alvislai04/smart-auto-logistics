package com.fyp.autologistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecordBlockchainEventRequest {

    @NotNull(message = "Shipment ID is required")
    private Long shipmentId;

    @NotNull(message = "Shipment event ID is required")
    private Long shipmentEventId;

    @NotBlank(message = "Event payload is required")
    private String eventPayload;

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getShipmentEventId() {
        return shipmentEventId;
    }

    public void setShipmentEventId(Long shipmentEventId) {
        this.shipmentEventId = shipmentEventId;
    }

    public String getEventPayload() {
        return eventPayload;
    }

    public void setEventPayload(String eventPayload) {
        this.eventPayload = eventPayload;
    }
}