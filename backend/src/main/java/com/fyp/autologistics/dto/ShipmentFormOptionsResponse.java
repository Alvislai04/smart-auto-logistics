package com.fyp.autologistics.dto;

import java.util.List;

public record ShipmentFormOptionsResponse(
        List<VehicleModelOptionResponse> vehicleModels,
        List<ShipmentPartyOptionResponse> manufacturerOrigins,
        List<ShipmentPartyOptionResponse> dealershipDestinations,
        List<MasterDataOptionResponse> logisticsStageOptions,
        List<MasterDataOptionResponse> workInProgressStatusOptions
) {
}
