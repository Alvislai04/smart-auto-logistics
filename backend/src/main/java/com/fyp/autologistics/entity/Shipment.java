package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.WorkInProgressStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
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

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private LocalDateTime estimatedArrival;

    private LocalDateTime estimatedDeparture;

    @Enumerated(EnumType.STRING)
    private LogisticsStage currentStage;

    private String stockLocation;

    @Enumerated(EnumType.STRING)
    private WorkInProgressStatus workInProgressStatus;

    private String responsiblePerson;

    private String responsibleContact;

    private String customerName;

    private String customerContact;

    @Column(length = 500)
    private String customerAddress;

    private String shipmentDocumentReference;

    private LocalDateTime receiptConfirmedAt;

    private String receiptConfirmedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Shipment() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ShipmentStatus.CREATED;
        }

        if (this.currentStage == null) {
            this.currentStage = LogisticsStage.MANUFACTURING;
        }

        if (this.workInProgressStatus == null) {
            this.workInProgressStatus = WorkInProgressStatus.NOT_STARTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getShipmentCode() {
        return shipmentCode;
    }

    public void setShipmentCode(String shipmentCode) {
        this.shipmentCode = shipmentCode;
    }

    public String getVehicleVin() {
        return vehicleVin;
    }

    public void setVehicleVin(String vehicleVin) {
        this.vehicleVin = vehicleVin;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getVehicleModelCode() {
        return vehicleModelCode;
    }

    public void setVehicleModelCode(String vehicleModelCode) {
        this.vehicleModelCode = vehicleModelCode;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getManufacturerKey() {
        return manufacturerKey;
    }

    public void setManufacturerKey(String manufacturerKey) {
        this.manufacturerKey = manufacturerKey;
    }

    public String getDealershipKey() {
        return dealershipKey;
    }

    public void setDealershipKey(String dealershipKey) {
        this.dealershipKey = dealershipKey;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(Double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public Double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(Double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public String getDealershipName() {
        return dealershipName;
    }

    public void setDealershipName(String dealershipName) {
        this.dealershipName = dealershipName;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(LocalDateTime estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public LocalDateTime getEstimatedDeparture() {
        return estimatedDeparture;
    }

    public void setEstimatedDeparture(LocalDateTime estimatedDeparture) {
        this.estimatedDeparture = estimatedDeparture;
    }

    public LogisticsStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(LogisticsStage currentStage) {
        this.currentStage = currentStage;
    }

    public String getStockLocation() {
        return stockLocation;
    }

    public void setStockLocation(String stockLocation) {
        this.stockLocation = stockLocation;
    }

    public WorkInProgressStatus getWorkInProgressStatus() {
        return workInProgressStatus;
    }

    public void setWorkInProgressStatus(WorkInProgressStatus workInProgressStatus) {
        this.workInProgressStatus = workInProgressStatus;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public String getResponsibleContact() {
        return responsibleContact;
    }

    public void setResponsibleContact(String responsibleContact) {
        this.responsibleContact = responsibleContact;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getShipmentDocumentReference() {
        return shipmentDocumentReference;
    }

    public void setShipmentDocumentReference(String shipmentDocumentReference) {
        this.shipmentDocumentReference = shipmentDocumentReference;
    }

    public LocalDateTime getReceiptConfirmedAt() {
        return receiptConfirmedAt;
    }

    public void setReceiptConfirmedAt(LocalDateTime receiptConfirmedAt) {
        this.receiptConfirmedAt = receiptConfirmedAt;
    }

    public String getReceiptConfirmedBy() {
        return receiptConfirmedBy;
    }

    public void setReceiptConfirmedBy(String receiptConfirmedBy) {
        this.receiptConfirmedBy = receiptConfirmedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
