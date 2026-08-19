package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.UserRole;

import java.time.LocalDateTime;

public record ShipmentPartyProfileMasterDataResponse(
        Long id,
        String partyKey,
        UserRole partyRole,
        String companyName,
        String operatingLocation,
        Double locationLatitude,
        Double locationLongitude,
        boolean active,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
