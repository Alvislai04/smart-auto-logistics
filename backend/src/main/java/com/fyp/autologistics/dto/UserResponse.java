package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.UserRole;

import java.time.LocalDateTime;

public class UserResponse {

    private final Long id;
    private final String fullName;
    private final String email;
    private final UserRole role;
    private final boolean active;
    private final String companyName;
    private final String operatingLocation;
    private final Double locationLatitude;
    private final Double locationLongitude;
    private final String partyProfileKey;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserResponse(
            Long id,
            String fullName,
            String email,
            UserRole role,
            boolean active,
            String companyName,
            String operatingLocation,
            Double locationLatitude,
            Double locationLongitude,
            String partyProfileKey,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.companyName = companyName;
        this.operatingLocation = operatingLocation;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.partyProfileKey = partyProfileKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getOperatingLocation() {
        return operatingLocation;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public String getPartyProfileKey() {
        return partyProfileKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
