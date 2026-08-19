package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.LogisticsStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_alert_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_alert_rules_scope",
                        columnNames = {"party_key", "vehicle_model_code", "logistics_stage"}
                ),
                @UniqueConstraint(
                        name = "uk_stock_alert_rules_rule_key",
                        columnNames = "rule_key"
                )
        }
)
public class StockAlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_key", nullable = false, unique = true, updatable = false)
    private String ruleKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "party_key",
            referencedColumnName = "party_key",
            nullable = false
    )
    private ShipmentPartyProfile partyProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "vehicle_model_code",
            referencedColumnName = "model_code",
            nullable = false
    )
    private VehicleModel vehicleModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "logistics_stage", nullable = false)
    private LogisticsStage logisticsStage;

    @Column(name = "minimum_quantity", nullable = false)
    private Integer minimumQuantity;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime lastEvaluatedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public StockAlertRule() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

    public ShipmentPartyProfile getPartyProfile() {
        return partyProfile;
    }

    public void setPartyProfile(ShipmentPartyProfile partyProfile) {
        this.partyProfile = partyProfile;
    }

    public VehicleModel getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(VehicleModel vehicleModel) {
        this.vehicleModel = vehicleModel;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(LocalDateTime lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
