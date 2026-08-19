package com.fyp.autologistics.dto;

import java.util.List;

public record StockMonitoringOverviewResponse(
        List<StockAlertRuleResponse> rules,
        List<StockPartyOptionResponse> partyOptions,
        List<VehicleModelOptionResponse> vehicleModels,
        List<MasterDataOptionResponse> logisticsStageOptions
) {
}
