package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.StockLevelStatus;
import com.fyp.autologistics.enums.UserRole;

import java.time.LocalDateTime;

public record StockAlertRuleResponse(
        Long id,
        String ruleKey,
        String partyKey,
        UserRole partyRole,
        String companyName,
        String vehicleModelCode,
        String vehicleModelName,
        LogisticsStage logisticsStage,
        Integer minimumQuantity,
        long currentQuantity,
        StockLevelStatus stockLevelStatus,
        boolean active,
        LocalDateTime lastEvaluatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
