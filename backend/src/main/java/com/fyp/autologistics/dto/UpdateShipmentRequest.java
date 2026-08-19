package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.WorkInProgressStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class UpdateShipmentRequest {

    @NotBlank(message = "Vehicle VIN is required")
    private String vehicleVin;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModelKey;

    @NotBlank(message = "Manufacturer origin is required")
    private String manufacturerKey;

    @NotBlank(message = "Dealership destination is required")
    private String dealershipKey;

    @NotNull(message = "Estimated arrival is required")
    private LocalDateTime estimatedArrival;

    @NotNull(message = "Estimated departure is required")
    private LocalDateTime estimatedDeparture;

    @NotNull(message = "Current logistics stage is required")
    private LogisticsStage currentStage;

    @NotBlank(message = "Stock location is required")
    private String stockLocation;

    @NotNull(message = "Work-in-progress status is required")
    private WorkInProgressStatus workInProgressStatus;

    @NotBlank(message = "Responsible person is required")
    private String responsiblePerson;

    @NotBlank(message = "Responsible contact is required")
    private String responsibleContact;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer contact is required")
    private String customerContact;

    @NotBlank(message = "Customer address is required")
    private String customerAddress;

    private String shipmentDocumentReference;

    public String getVehicleVin() {
        return vehicleVin;
    }

    public void setVehicleVin(String vehicleVin) {
        this.vehicleVin = vehicleVin;
    }

    public String getVehicleModelKey() {
        return vehicleModelKey;
    }

    public void setVehicleModelKey(String vehicleModelKey) {
        this.vehicleModelKey = vehicleModelKey;
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
}
