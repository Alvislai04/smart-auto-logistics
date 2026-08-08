package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.WorkInProgressStatus;

import java.time.LocalDateTime;

public class ShipmentResponse {

    private Long id;
    private String shipmentCode;
    private String vehicleVin;
    private String vehicleModelCode;
    private String vehicleModel;
    private String manufacturerKey;
    private String dealershipKey;
    private String origin;
    private String destination;
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String dealershipName;
    private ShipmentStatus status;
    private LocalDateTime estimatedArrival;
    private LocalDateTime estimatedDeparture;
    private LogisticsStage currentStage;
    private String stockLocation;
    private WorkInProgressStatus workInProgressStatus;
    private String responsiblePerson;
    private String responsibleContact;
    private String customerName;
    private String customerContact;
    private String customerAddress;
    private String shipmentDocumentReference;
    private LocalDateTime receiptConfirmedAt;
    private String receiptConfirmedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShipmentResponse(
            Long id,
            String shipmentCode,
            String vehicleVin,
            String vehicleModelCode,
            String vehicleModel,
            String manufacturerKey,
            String dealershipKey,
            String origin,
            String destination,
            Double originLatitude,
            Double originLongitude,
            Double destinationLatitude,
            Double destinationLongitude,
            String dealershipName,
            ShipmentStatus status,
            LocalDateTime estimatedArrival,
            LocalDateTime estimatedDeparture,
            LogisticsStage currentStage,
            String stockLocation,
            WorkInProgressStatus workInProgressStatus,
            String responsiblePerson,
            String responsibleContact,
            String customerName,
            String customerContact,
            String customerAddress,
            String shipmentDocumentReference,
            LocalDateTime receiptConfirmedAt,
            String receiptConfirmedBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shipmentCode = shipmentCode;
        this.vehicleVin = vehicleVin;
        this.vehicleModelCode = vehicleModelCode;
        this.vehicleModel = vehicleModel;
        this.manufacturerKey = manufacturerKey;
        this.dealershipKey = dealershipKey;
        this.origin = origin;
        this.destination = destination;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.dealershipName = dealershipName;
        this.status = status;
        this.estimatedArrival = estimatedArrival;
        this.estimatedDeparture = estimatedDeparture;
        this.currentStage = currentStage;
        this.stockLocation = stockLocation;
        this.workInProgressStatus = workInProgressStatus;
        this.responsiblePerson = responsiblePerson;
        this.responsibleContact = responsibleContact;
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.customerAddress = customerAddress;
        this.shipmentDocumentReference = shipmentDocumentReference;
        this.receiptConfirmedAt = receiptConfirmedAt;
        this.receiptConfirmedBy = receiptConfirmedBy;
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

    public String getVehicleModelCode() {
        return vehicleModelCode;
    }

    public String getManufacturerKey() {
        return manufacturerKey;
    }

    public String getDealershipKey() {
        return dealershipKey;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public Double getOriginLatitude() {
        return originLatitude;
    }

    public Double getOriginLongitude() {
        return originLongitude;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
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

    public LocalDateTime getEstimatedDeparture() {
        return estimatedDeparture;
    }

    public LogisticsStage getCurrentStage() {
        return currentStage;
    }

    public String getStockLocation() {
        return stockLocation;
    }

    public WorkInProgressStatus getWorkInProgressStatus() {
        return workInProgressStatus;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public String getResponsibleContact() {
        return responsibleContact;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getShipmentDocumentReference() {
        return shipmentDocumentReference;
    }

    public LocalDateTime getReceiptConfirmedAt() {
        return receiptConfirmedAt;
    }

    public String getReceiptConfirmedBy() {
        return receiptConfirmedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
