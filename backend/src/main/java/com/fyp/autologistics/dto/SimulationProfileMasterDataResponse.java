package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.ConditionStatus;

import java.time.LocalDateTime;

public record SimulationProfileMasterDataResponse(
        Long id,
        String profileKey,
        String displayName,
        ConditionStatus conditionStatus,
        Double minSpeed,
        Double maxSpeed,
        Integer weight,
        Boolean routeDeviationEnabled,
        Double routeDeviationLatitudeOffset,
        Boolean active,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
