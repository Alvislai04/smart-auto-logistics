package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.LogisticsStage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockAlertRuleRequest {

    @NotBlank(message = "Company is required.")
    private String partyKey;

    @NotBlank(message = "Vehicle model is required.")
    private String vehicleModelCode;

    @NotNull(message = "Logistics stage is required.")
    private LogisticsStage logisticsStage;

    @NotNull(message = "Minimum quantity is required.")
    @Min(value = 1, message = "Minimum quantity must be at least 1.")
    private Integer minimumQuantity;

    @NotNull(message = "Active status is required.")
    private Boolean active;

    public String getPartyKey() {
        return partyKey;
    }

    public void setPartyKey(String partyKey) {
        this.partyKey = partyKey;
    }

    public String getVehicleModelCode() {
        return vehicleModelCode;
    }

    public void setVehicleModelCode(String vehicleModelCode) {
        this.vehicleModelCode = vehicleModelCode;
    }

    public LogisticsStage getLogisticsStage() {
        return logisticsStage;
    }

    public void setLogisticsStage(LogisticsStage logisticsStage) {
        this.logisticsStage = logisticsStage;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
