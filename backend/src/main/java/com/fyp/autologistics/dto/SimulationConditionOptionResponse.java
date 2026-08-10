package com.fyp.autologistics.dto;

public class SimulationConditionOptionResponse {

    private String conditionKey;
    private String displayName;

    public SimulationConditionOptionResponse(String conditionKey, String displayName) {
        this.conditionKey = conditionKey;
        this.displayName = displayName;
    }

    public String getConditionKey() {
        return conditionKey;
    }

    public String getDisplayName() {
        return displayName;
    }
}
