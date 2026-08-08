package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.UserRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shipment_party_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shipment_party_profiles_party_key", columnNames = "party_key")
        }
)
public class ShipmentPartyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "party_key", nullable = false, unique = true)
    private String partyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole partyRole;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String operatingLocation;

    @Column(nullable = false)
    private Double locationLatitude;

    @Column(nullable = false)
    private Double locationLongitude;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ShipmentPartyProfile() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }

        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getPartyKey() {
        return partyKey;
    }

    public void setPartyKey(String partyKey) {
        this.partyKey = partyKey;
    }

    public UserRole getPartyRole() {
        return partyRole;
    }

    public void setPartyRole(UserRole partyRole) {
        this.partyRole = partyRole;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOperatingLocation() {
        return operatingLocation;
    }

    public void setOperatingLocation(String operatingLocation) {
        this.operatingLocation = operatingLocation;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
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
