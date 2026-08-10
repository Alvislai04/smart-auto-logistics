package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ConditionStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StartSimulationSessionRequest {

    @NotNull(message = "Simulation interval is required")
    @Min(value = 5, message = "Simulation interval must be at least 5 seconds")
    @Max(value = 3600, message = "Simulation interval cannot exceed 3600 seconds")
    private Integer intervalSeconds;

    @NotNull(message = "Simulation speed is required")
    @DecimalMin(value = "0.0", message = "Simulation speed cannot be negative")
    @DecimalMax(value = "160.0", message = "Simulation speed cannot exceed 160 km/h")
    private Double speed;

    private ConditionStatus conditionStatus;

    @Min(value = 1, message = "Maximum generated events must be at least 1")
    @Max(value = 500, message = "Maximum generated events cannot exceed 500")
    private Integer maxEvents;

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public ConditionStatus getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(ConditionStatus conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public Integer getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(Integer maxEvents) {
        this.maxEvents = maxEvents;
    }
}
