package com.fyp.autologistics.dto;

import java.time.LocalDateTime;

public record VehicleModelMasterDataResponse(
        Long id,
        String modelCode,
        String brand,
        String displayName,
        boolean active,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
