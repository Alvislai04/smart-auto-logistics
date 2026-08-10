package com.fyp.autologistics.dto;

import java.util.List;

public class SimulationOptionsResponse {

    private List<SimulationConditionOptionResponse> conditionOptions;
    private int defaultIntervalSeconds;
    private int minIntervalSeconds;
    private int maxIntervalSeconds;
    private double defaultSpeed;
    private double minSpeed;
    private double maxSpeed;
    private int maxGeneratedEvents;

    public SimulationOptionsResponse(
            List<SimulationConditionOptionResponse> conditionOptions,
            int defaultIntervalSeconds,
            int minIntervalSeconds,
            int maxIntervalSeconds,
            double defaultSpeed,
            double minSpeed,
            double maxSpeed,
            int maxGeneratedEvents
    ) {
        this.conditionOptions = conditionOptions;
        this.defaultIntervalSeconds = defaultIntervalSeconds;
        this.minIntervalSeconds = minIntervalSeconds;
        this.maxIntervalSeconds = maxIntervalSeconds;
        this.defaultSpeed = defaultSpeed;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.maxGeneratedEvents = maxGeneratedEvents;
    }

    public List<SimulationConditionOptionResponse> getConditionOptions() {
        return conditionOptions;
    }

    public int getDefaultIntervalSeconds() {
        return defaultIntervalSeconds;
    }

    public int getMinIntervalSeconds() {
        return minIntervalSeconds;
    }

    public int getMaxIntervalSeconds() {
        return maxIntervalSeconds;
    }

    public double getDefaultSpeed() {
        return defaultSpeed;
    }

    public double getMinSpeed() {
        return minSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public int getMaxGeneratedEvents() {
        return maxGeneratedEvents;
    }
}
