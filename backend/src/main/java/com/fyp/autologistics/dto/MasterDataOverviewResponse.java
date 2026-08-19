package com.fyp.autologistics.dto;

import java.util.List;

public record MasterDataOverviewResponse(
        List<VehicleModelMasterDataResponse> vehicleModels,
        List<ShipmentPartyProfileMasterDataResponse> partyProfiles,
        List<SimulationProfileMasterDataResponse> simulationProfiles,
        List<MasterDataOptionResponse> partyRoleOptions,
        List<MasterDataOptionResponse> conditionStatusOptions
) {
}
