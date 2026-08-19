package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.*;
import com.fyp.autologistics.service.AdminMasterDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/master-data")
public class AdminMasterDataController {

    private final AdminMasterDataService adminMasterDataService;

    public AdminMasterDataController(AdminMasterDataService adminMasterDataService) {
        this.adminMasterDataService = adminMasterDataService;
    }

    @GetMapping
    public MasterDataOverviewResponse getOverview() {
        return adminMasterDataService.getOverview();
    }

    @PostMapping("/vehicle-models")
    public VehicleModelMasterDataResponse createVehicleModel(
            @Valid @RequestBody VehicleModelMasterDataRequest request
    ) {
        return adminMasterDataService.createVehicleModel(request);
    }

    @PutMapping("/vehicle-models/{vehicleModelId}")
    public VehicleModelMasterDataResponse updateVehicleModel(
            @PathVariable Long vehicleModelId,
            @Valid @RequestBody VehicleModelMasterDataRequest request
    ) {
        return adminMasterDataService.updateVehicleModel(vehicleModelId, request);
    }

    @PostMapping("/party-profiles")
    public ShipmentPartyProfileMasterDataResponse createPartyProfile(
            @Valid @RequestBody ShipmentPartyProfileMasterDataRequest request
    ) {
        return adminMasterDataService.createPartyProfile(request);
    }

    @PutMapping("/party-profiles/{profileId}")
    public ShipmentPartyProfileMasterDataResponse updatePartyProfile(
            @PathVariable Long profileId,
            @Valid @RequestBody ShipmentPartyProfileMasterDataRequest request
    ) {
        return adminMasterDataService.updatePartyProfile(profileId, request);
    }

    @PostMapping("/simulation-profiles")
    public SimulationProfileMasterDataResponse createSimulationProfile(
            @Valid @RequestBody SimulationProfileMasterDataRequest request
    ) {
        return adminMasterDataService.createSimulationProfile(request);
    }

    @PutMapping("/simulation-profiles/{profileId}")
    public SimulationProfileMasterDataResponse updateSimulationProfile(
            @PathVariable Long profileId,
            @Valid @RequestBody SimulationProfileMasterDataRequest request
    ) {
        return adminMasterDataService.updateSimulationProfile(profileId, request);
    }
}
