package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.*;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.SimulationProfile;
import com.fyp.autologistics.entity.UserRoleOption;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.ConditionStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.SimulationProfileRepository;
import com.fyp.autologistics.repository.UserRoleOptionRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AdminMasterDataService {

    private static final Pattern MASTER_KEY_PATTERN =
            Pattern.compile("^[A-Z0-9][A-Z0-9_-]{1,63}$");

    private final VehicleModelRepository vehicleModelRepository;
    private final ShipmentPartyProfileRepository shipmentPartyProfileRepository;
    private final SimulationProfileRepository simulationProfileRepository;
    private final UserRoleOptionRepository userRoleOptionRepository;

    public AdminMasterDataService(
            VehicleModelRepository vehicleModelRepository,
            ShipmentPartyProfileRepository shipmentPartyProfileRepository,
            SimulationProfileRepository simulationProfileRepository,
            UserRoleOptionRepository userRoleOptionRepository
    ) {
        this.vehicleModelRepository = vehicleModelRepository;
        this.shipmentPartyProfileRepository = shipmentPartyProfileRepository;
        this.simulationProfileRepository = simulationProfileRepository;
        this.userRoleOptionRepository = userRoleOptionRepository;
    }

    @Transactional(readOnly = true)
    public MasterDataOverviewResponse getOverview() {
        return new MasterDataOverviewResponse(
                getVehicleModels(),
                getPartyProfiles(),
                getSimulationProfiles(),
                getPartyRoleOptions(),
                getConditionStatusOptions()
        );
    }

    @Transactional(readOnly = true)
    public List<VehicleModelMasterDataResponse> getVehicleModels() {
        return vehicleModelRepository.findAllByOrderBySortOrderAscDisplayNameAsc()
                .stream()
                .map(this::mapVehicleModel)
                .toList();
    }

    @Transactional
    public VehicleModelMasterDataResponse createVehicleModel(
            VehicleModelMasterDataRequest request
    ) {
        String modelCode = normalizeMasterKey(request.getModelCode(), "Vehicle model code");
        ensureVehicleModelCodeAvailable(modelCode, null);

        VehicleModel vehicleModel = new VehicleModel();
        vehicleModel.setModelCode(modelCode);
        applyVehicleModelFields(vehicleModel, request);

        return mapVehicleModel(vehicleModelRepository.save(vehicleModel));
    }

    @Transactional
    public VehicleModelMasterDataResponse updateVehicleModel(
            Long vehicleModelId,
            VehicleModelMasterDataRequest request
    ) {
        VehicleModel vehicleModel = findVehicleModel(vehicleModelId);
        validateStableKey(
                vehicleModel.getModelCode(),
                request.getModelCode(),
                "Vehicle model code"
        );

        applyVehicleModelFields(vehicleModel, request);
        return mapVehicleModel(vehicleModelRepository.save(vehicleModel));
    }

    @Transactional(readOnly = true)
    public List<ShipmentPartyProfileMasterDataResponse> getPartyProfiles() {
        return shipmentPartyProfileRepository.findAllByOrderBySortOrderAscCompanyNameAsc()
                .stream()
                .map(this::mapPartyProfile)
                .toList();
    }

    @Transactional
    public ShipmentPartyProfileMasterDataResponse createPartyProfile(
            ShipmentPartyProfileMasterDataRequest request
    ) {
        String partyKey = normalizeMasterKey(request.getPartyKey(), "Party key");
        ensurePartyKeyAvailable(partyKey, null);

        ShipmentPartyProfile profile = new ShipmentPartyProfile();
        profile.setPartyKey(partyKey);
        applyPartyProfileFields(profile, request);

        return mapPartyProfile(shipmentPartyProfileRepository.save(profile));
    }

    @Transactional
    public ShipmentPartyProfileMasterDataResponse updatePartyProfile(
            Long profileId,
            ShipmentPartyProfileMasterDataRequest request
    ) {
        ShipmentPartyProfile profile = findPartyProfile(profileId);
        validateStableKey(profile.getPartyKey(), request.getPartyKey(), "Party key");

        applyPartyProfileFields(profile, request);
        return mapPartyProfile(shipmentPartyProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<SimulationProfileMasterDataResponse> getSimulationProfiles() {
        return simulationProfileRepository.findAllByOrderBySortOrderAscDisplayNameAsc()
                .stream()
                .map(this::mapSimulationProfile)
                .toList();
    }

    @Transactional
    public SimulationProfileMasterDataResponse createSimulationProfile(
            SimulationProfileMasterDataRequest request
    ) {
        String profileKey = normalizeMasterKey(request.getProfileKey(), "Simulation profile key");
        ensureSimulationProfileKeyAvailable(profileKey, null);

        SimulationProfile profile = new SimulationProfile();
        profile.setProfileKey(profileKey);
        applySimulationProfileFields(profile, request);

        return mapSimulationProfile(simulationProfileRepository.save(profile));
    }

    @Transactional
    public SimulationProfileMasterDataResponse updateSimulationProfile(
            Long profileId,
            SimulationProfileMasterDataRequest request
    ) {
        SimulationProfile profile = findSimulationProfile(profileId);
        validateStableKey(
                profile.getProfileKey(),
                request.getProfileKey(),
                "Simulation profile key"
        );

        applySimulationProfileFields(profile, request);
        return mapSimulationProfile(simulationProfileRepository.save(profile));
    }

    private void applyVehicleModelFields(
            VehicleModel vehicleModel,
            VehicleModelMasterDataRequest request
    ) {
        vehicleModel.setBrand(normalizeRequiredText(request.getBrand(), "Vehicle brand"));
        vehicleModel.setDisplayName(normalizeRequiredText(
                request.getDisplayName(),
                "Vehicle model display name"
        ));
        vehicleModel.setActive(request.getActive() == null || request.getActive());
        vehicleModel.setSortOrder(normalizeSortOrder(request.getSortOrder()));
    }

    private void applyPartyProfileFields(
            ShipmentPartyProfile profile,
            ShipmentPartyProfileMasterDataRequest request
    ) {
        UserRole partyRole = validatePartyRole(request.getPartyRole());

        profile.setPartyRole(partyRole);
        profile.setCompanyName(normalizeRequiredText(request.getCompanyName(), "Company name"));
        profile.setOperatingLocation(normalizeRequiredText(
                request.getOperatingLocation(),
                "Operating location"
        ));
        profile.setLocationLatitude(validateLatitude(request.getLocationLatitude()));
        profile.setLocationLongitude(validateLongitude(request.getLocationLongitude()));
        profile.setActive(request.getActive() == null || request.getActive());
        profile.setSortOrder(normalizeSortOrder(request.getSortOrder()));
    }

    private void applySimulationProfileFields(
            SimulationProfile profile,
            SimulationProfileMasterDataRequest request
    ) {
        double minSpeed = validateNonNegativeSpeed(request.getMinSpeed(), "Minimum speed");
        double maxSpeed = validateNonNegativeSpeed(request.getMaxSpeed(), "Maximum speed");

        if (maxSpeed < minSpeed) {
            throw new BadRequestException("Maximum speed must be greater than or equal to minimum speed.");
        }

        Integer weight = request.getWeight();

        if (weight == null || weight < 1 || weight > 1000) {
            throw new BadRequestException("Simulation profile weight must be between 1 and 1000.");
        }

        profile.setDisplayName(normalizeRequiredText(
                request.getDisplayName(),
                "Simulation profile display name"
        ));
        profile.setConditionStatus(validateConditionStatus(request.getConditionStatus()));
        profile.setMinSpeed(minSpeed);
        profile.setMaxSpeed(maxSpeed);
        profile.setWeight(weight);
        profile.setRouteDeviationEnabled(Boolean.TRUE.equals(
                request.getRouteDeviationEnabled()
        ));
        profile.setRouteDeviationLatitudeOffset(validateRouteDeviationOffset(
                request.getRouteDeviationLatitudeOffset()
        ));
        profile.setActive(request.getActive() == null || request.getActive());
        profile.setSortOrder(normalizeSortOrder(request.getSortOrder()));
    }

    private VehicleModel findVehicleModel(Long vehicleModelId) {
        return vehicleModelRepository.findById(vehicleModelId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle model not found."));
    }

    private ShipmentPartyProfile findPartyProfile(Long profileId) {
        return shipmentPartyProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment party profile not found."));
    }

    private SimulationProfile findSimulationProfile(Long profileId) {
        return simulationProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation profile not found."));
    }

    private void ensureVehicleModelCodeAvailable(String modelCode, Long currentId) {
        Optional<VehicleModel> existingVehicleModel =
                vehicleModelRepository.findByModelCode(modelCode);

        if (existingVehicleModel.isPresent()
                && !existingVehicleModel.get().getId().equals(currentId)) {
            throw new BadRequestException("Vehicle model code is already configured.");
        }
    }

    private void ensurePartyKeyAvailable(String partyKey, Long currentId) {
        Optional<ShipmentPartyProfile> existingProfile =
                shipmentPartyProfileRepository.findByPartyKey(partyKey);

        if (existingProfile.isPresent() && !existingProfile.get().getId().equals(currentId)) {
            throw new BadRequestException("Party key is already configured.");
        }
    }

    private void ensureSimulationProfileKeyAvailable(String profileKey, Long currentId) {
        Optional<SimulationProfile> existingProfile =
                simulationProfileRepository.findByProfileKey(profileKey);

        if (existingProfile.isPresent() && !existingProfile.get().getId().equals(currentId)) {
            throw new BadRequestException("Simulation profile key is already configured.");
        }
    }

    private void validateStableKey(
            String existingKey,
            String requestedKey,
            String fieldName
    ) {
        String normalizedRequestedKey = normalizeMasterKey(requestedKey, fieldName);

        if (!existingKey.equals(normalizedRequestedKey)) {
            throw new BadRequestException(fieldName + " cannot be changed after creation.");
        }
    }

    private UserRole validatePartyRole(UserRole partyRole) {
        if (partyRole == null) {
            throw new BadRequestException("Party role is required.");
        }

        if (partyRole == UserRole.ADMIN) {
            throw new BadRequestException("Admin is not a shipment party role.");
        }

        return partyRole;
    }

    private ConditionStatus validateConditionStatus(ConditionStatus conditionStatus) {
        if (conditionStatus == null) {
            throw new BadRequestException("Condition status is required.");
        }

        return conditionStatus;
    }

    private Double validateLatitude(Double latitude) {
        if (latitude == null || latitude < -90.0 || latitude > 90.0) {
            throw new BadRequestException("Latitude must be between -90 and 90.");
        }

        return latitude;
    }

    private Double validateLongitude(Double longitude) {
        if (longitude == null || longitude < -180.0 || longitude > 180.0) {
            throw new BadRequestException("Longitude must be between -180 and 180.");
        }

        return longitude;
    }

    private double validateNonNegativeSpeed(Double speed, String fieldName) {
        if (speed == null || speed < 0.0 || speed > 250.0) {
            throw new BadRequestException(fieldName + " must be between 0 and 250 km/h.");
        }

        return speed;
    }

    private double validateRouteDeviationOffset(Double offset) {
        double resolvedOffset = offset == null ? 0.0 : offset;

        if (resolvedOffset < -5.0 || resolvedOffset > 5.0) {
            throw new BadRequestException("Route deviation latitude offset must be between -5 and 5.");
        }

        return resolvedOffset;
    }

    private Integer normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null) {
            return 0;
        }

        if (sortOrder < 0 || sortOrder > 100000) {
            throw new BadRequestException("Sort order must be between 0 and 100000.");
        }

        return sortOrder;
    }

    private String normalizeMasterKey(String value, String fieldName) {
        String normalizedValue = normalizeRequiredText(value, fieldName)
                .replaceAll("\\s+", "-")
                .toUpperCase(Locale.ROOT);

        if (!MASTER_KEY_PATTERN.matcher(normalizedValue).matches()) {
            throw new BadRequestException(
                    fieldName + " must be 2 to 64 characters using letters, numbers, dashes, or underscores."
            );
        }

        return normalizedValue;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(fieldName + " is required.");
        }

        return value.trim();
    }

    private List<MasterDataOptionResponse> getPartyRoleOptions() {
        return userRoleOptionRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc()
                .stream()
                .filter(roleOption -> isShipmentPartyRole(roleOption.getRoleKey()))
                .map(this::mapRoleOption)
                .toList();
    }

    private boolean isShipmentPartyRole(String roleKey) {
        if (!StringUtils.hasText(roleKey)) {
            return false;
        }

        try {
            return UserRole.valueOf(roleKey.trim().toUpperCase(Locale.ROOT)) != UserRole.ADMIN;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private List<MasterDataOptionResponse> getConditionStatusOptions() {
        return Arrays.stream(ConditionStatus.values())
                .map(conditionStatus -> new MasterDataOptionResponse(
                        conditionStatus.name(),
                        toDisplayLabel(conditionStatus.name())
                ))
                .toList();
    }

    private MasterDataOptionResponse mapRoleOption(UserRoleOption roleOption) {
        return new MasterDataOptionResponse(
                roleOption.getRoleKey(),
                roleOption.getDisplayName()
        );
    }

    private VehicleModelMasterDataResponse mapVehicleModel(VehicleModel vehicleModel) {
        return new VehicleModelMasterDataResponse(
                vehicleModel.getId(),
                vehicleModel.getModelCode(),
                vehicleModel.getBrand(),
                vehicleModel.getDisplayName(),
                vehicleModel.isActive(),
                vehicleModel.getSortOrder(),
                vehicleModel.getCreatedAt(),
                vehicleModel.getUpdatedAt()
        );
    }

    private ShipmentPartyProfileMasterDataResponse mapPartyProfile(
            ShipmentPartyProfile profile
    ) {
        return new ShipmentPartyProfileMasterDataResponse(
                profile.getId(),
                profile.getPartyKey(),
                profile.getPartyRole(),
                profile.getCompanyName(),
                profile.getOperatingLocation(),
                profile.getLocationLatitude(),
                profile.getLocationLongitude(),
                profile.isActive(),
                profile.getSortOrder(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private SimulationProfileMasterDataResponse mapSimulationProfile(
            SimulationProfile profile
    ) {
        return new SimulationProfileMasterDataResponse(
                profile.getId(),
                profile.getProfileKey(),
                profile.getDisplayName(),
                profile.getConditionStatus(),
                profile.getMinSpeed(),
                profile.getMaxSpeed(),
                profile.getWeight(),
                profile.getRouteDeviationEnabled(),
                profile.getRouteDeviationLatitudeOffset(),
                profile.getActive(),
                profile.getSortOrder(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
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
