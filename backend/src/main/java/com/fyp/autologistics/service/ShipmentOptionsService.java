package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.ShipmentFormOptionsResponse;
import com.fyp.autologistics.dto.ShipmentPartyOptionResponse;
import com.fyp.autologistics.dto.VehicleModelOptionResponse;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipmentOptionsService {

    private final VehicleModelRepository vehicleModelRepository;
    private final ShipmentPartyProfileRepository shipmentPartyProfileRepository;

    public ShipmentOptionsService(
            VehicleModelRepository vehicleModelRepository,
            ShipmentPartyProfileRepository shipmentPartyProfileRepository
    ) {
        this.vehicleModelRepository = vehicleModelRepository;
        this.shipmentPartyProfileRepository = shipmentPartyProfileRepository;
    }

    public ShipmentFormOptionsResponse getShipmentFormOptions() {
        return new ShipmentFormOptionsResponse(
                getVehicleModelOptions(),
                getPartyOptions(UserRole.MANUFACTURER),
                getPartyOptions(UserRole.DEALERSHIP)
        );
    }

    private List<VehicleModelOptionResponse> getVehicleModelOptions() {
        return vehicleModelRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc()
                .stream()
                .map(this::mapVehicleModelToOption)
                .toList();
    }

    private List<ShipmentPartyOptionResponse> getPartyOptions(UserRole role) {
        return shipmentPartyProfileRepository
                .findByPartyRoleAndActiveTrueOrderBySortOrderAscCompanyNameAsc(role)
                .stream()
                .map(this::mapToOption)
                .toList();
    }

    private VehicleModelOptionResponse mapVehicleModelToOption(VehicleModel vehicleModel) {
        return new VehicleModelOptionResponse(
                vehicleModel.getModelCode(),
                vehicleModel.getBrand(),
                vehicleModel.getDisplayName()
        );
    }

    private ShipmentPartyOptionResponse mapToOption(ShipmentPartyProfile profile) {
        return new ShipmentPartyOptionResponse(
                profile.getPartyKey(),
                profile.getCompanyName(),
                profile.getOperatingLocation(),
                profile.getLocationLatitude(),
                profile.getLocationLongitude()
        );
    }
}
