package com.fyp.autologistics.dto;

import jakarta.validation.constraints.NotNull;

public class SimulateTrackingRequest {

    @NotNull(message = "Current latitude is required")
    private Double currentLatitude;

    @NotNull(message = "Current longitude is required")
    private Double currentLongitude;

    private Double speed;

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}