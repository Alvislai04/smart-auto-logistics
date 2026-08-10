package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.SimulationSessionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_sessions")
public class SimulationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SimulationSessionStatus status;

    @Column(nullable = false)
    private Integer intervalSeconds;

    @Column(nullable = false)
    private Double speed;

    @Enumerated(EnumType.STRING)
    private ConditionStatus conditionStatus;

    @Column(nullable = false)
    private Double currentLatitude;

    @Column(nullable = false)
    private Double currentLongitude;

    private LocalDateTime nextRunAt;

    private LocalDateTime lastRunAt;

    @Column(nullable = false)
    private Integer generatedEvents;

    private Integer maxEvents;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public SimulationSession() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.status == null) {
            this.status = SimulationSessionStatus.ACTIVE;
        }

        if (this.generatedEvents == null) {
            this.generatedEvents = 0;
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

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public SimulationSessionStatus getStatus() {
        return status;
    }

    public void setStatus(SimulationSessionStatus status) {
        this.status = status;
    }

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

    public LocalDateTime getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(LocalDateTime nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public LocalDateTime getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(LocalDateTime lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public Integer getGeneratedEvents() {
        return generatedEvents;
    }

    public void setGeneratedEvents(Integer generatedEvents) {
        this.generatedEvents = generatedEvents;
    }

    public Integer getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(Integer maxEvents) {
        this.maxEvents = maxEvents;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
