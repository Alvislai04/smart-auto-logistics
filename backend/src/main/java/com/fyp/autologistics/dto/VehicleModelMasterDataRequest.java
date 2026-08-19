package com.fyp.autologistics.dto;

import jakarta.validation.constraints.NotBlank;

public class VehicleModelMasterDataRequest {

    @NotBlank(message = "Vehicle model code is required")
    private String modelCode;

    @NotBlank(message = "Vehicle brand is required")
    private String brand;

    @NotBlank(message = "Vehicle model display name is required")
    private String displayName;

    private Boolean active;
    private Integer sortOrder;

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
