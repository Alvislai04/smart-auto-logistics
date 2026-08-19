package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShipmentPartyProfileMasterDataRequest {

    @NotBlank(message = "Party key is required")
    private String partyKey;

    @NotNull(message = "Party role is required")
    private UserRole partyRole;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Operating location is required")
    private String operatingLocation;

    @NotNull(message = "Latitude is required")
    private Double locationLatitude;

    @NotNull(message = "Longitude is required")
    private Double locationLongitude;

    private Boolean active;
    private Integer sortOrder;

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
}
