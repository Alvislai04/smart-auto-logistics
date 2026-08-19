package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ConditionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SimulationProfileMasterDataRequest {

    @NotBlank(message = "Simulation profile key is required")
    private String profileKey;

    @NotBlank(message = "Simulation profile display name is required")
    private String displayName;

    @NotNull(message = "Condition status is required")
    private ConditionStatus conditionStatus;

    @NotNull(message = "Minimum speed is required")
    private Double minSpeed;

    @NotNull(message = "Maximum speed is required")
    private Double maxSpeed;

    @NotNull(message = "Weight is required")
    private Integer weight;

    private Boolean routeDeviationEnabled;
    private Double routeDeviationLatitudeOffset;
    private Boolean active;
    private Integer sortOrder;

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ConditionStatus getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(ConditionStatus conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public Double getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(Double minSpeed) {
        this.minSpeed = minSpeed;
    }

    public Double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(Double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Boolean getRouteDeviationEnabled() {
        return routeDeviationEnabled;
    }

    public void setRouteDeviationEnabled(Boolean routeDeviationEnabled) {
        this.routeDeviationEnabled = routeDeviationEnabled;
    }

    public Double getRouteDeviationLatitudeOffset() {
        return routeDeviationLatitudeOffset;
    }

    public void setRouteDeviationLatitudeOffset(Double routeDeviationLatitudeOffset) {
        this.routeDeviationLatitudeOffset = routeDeviationLatitudeOffset;
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
