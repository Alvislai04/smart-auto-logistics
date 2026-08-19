package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.ConditionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_profiles")
public class SimulationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String profileKey;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionStatus conditionStatus;

    @Column(nullable = false)
    private Double minSpeed;

    @Column(nullable = false)
    private Double maxSpeed;

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false)
    private Boolean routeDeviationEnabled;

    @Column(nullable = false)
    private Double routeDeviationLatitudeOffset;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public SimulationProfile() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.weight == null) {
            this.weight = 1;
        }

        if (this.routeDeviationEnabled == null) {
            this.routeDeviationEnabled = false;
        }

        if (this.routeDeviationLatitudeOffset == null) {
            this.routeDeviationLatitudeOffset = 0.0;
        }

        if (this.active == null) {
            this.active = true;
        }

        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
