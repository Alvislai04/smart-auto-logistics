package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.UserRole;

public class LoginResponse {

    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private UserRole role;
    private String companyName;
    private String operatingLocation;
    private Double locationLatitude;
    private Double locationLongitude;
    private String partyProfileKey;

    public LoginResponse(
            String token,
            Long userId,
            String fullName,
            String email,
            UserRole role,
            String companyName,
            String operatingLocation,
            Double locationLatitude,
            Double locationLongitude,
            String partyProfileKey
    ) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.companyName = companyName;
        this.operatingLocation = operatingLocation;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.partyProfileKey = partyProfileKey;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
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
}
