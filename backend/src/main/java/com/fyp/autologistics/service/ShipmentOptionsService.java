package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.ShipmentFormOptionsResponse;
import com.fyp.autologistics.dto.ShipmentPartyOptionResponse;
import com.fyp.autologistics.dto.MasterDataOptionResponse;
import com.fyp.autologistics.dto.VehicleModelOptionResponse;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.enums.WorkInProgressStatus;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
                getPartyOptions(UserRole.DEALERSHIP),
                getLogisticsStageOptions(),
                getWorkInProgressStatusOptions()
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

    private List<MasterDataOptionResponse> getLogisticsStageOptions() {
        return Arrays.stream(LogisticsStage.values())
                .map(stage -> new MasterDataOptionResponse(
                        stage.name(),
                        toDisplayLabel(stage.name())
                ))
                .toList();
    }

    private List<MasterDataOptionResponse> getWorkInProgressStatusOptions() {
        return Arrays.stream(WorkInProgressStatus.values())
                .map(status -> new MasterDataOptionResponse(
                        status.name(),
                        toDisplayLabel(status.name())
                ))
                .toList();
    }

    private String toDisplayLabel(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).split("_");
        StringBuilder label = new StringBuilder();

        for (String word : words) {
            if (label.length() > 0) {
                label.append(' ');
            }

            label.append(Character.toUpperCase(word.charAt(0)));
            label.append(word.substring(1));
        }

        return label.toString();
    }
}
