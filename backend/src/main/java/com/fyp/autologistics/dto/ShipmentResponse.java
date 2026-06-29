package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ShipmentStatus;

import java.time.LocalDateTime;

public class ShipmentResponse {

    private Long id;
    private String shipmentCode;
    private String vehicleVin;
    private String vehicleModel;
    private String origin;
    private String destination;
    private String dealershipName;
    private ShipmentStatus status;
    private LocalDateTime estimatedArrival;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShipmentResponse(
            Long id,
            String shipmentCode,
            String vehicleVin,
            String vehicleModel,
            String origin,
            String destination,
            String dealershipName,
            ShipmentStatus status,
            LocalDateTime estimatedArrival,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shipmentCode = shipmentCode;
        this.vehicleVin = vehicleVin;
        this.vehicleModel = vehicleModel;
        this.origin = origin;
        this.destination = destination;
        this.dealershipName = dealershipName;
        this.status = status;
        this.estimatedArrival = estimatedArrival;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getShipmentCode() {
        return shipmentCode;
    }

    public String getVehicleVin() {
        return vehicleVin;
    }

    public String getVehicleModel() {
        return vehicleModel;
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

    public ShipmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getEstimatedArrival() {
        return estimatedArrival;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}