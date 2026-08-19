package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.MasterDataOptionResponse;
import com.fyp.autologistics.dto.StockAlertRuleRequest;
import com.fyp.autologistics.dto.StockAlertRuleResponse;
import com.fyp.autologistics.dto.StockMonitoringOverviewResponse;
import com.fyp.autologistics.dto.StockPartyOptionResponse;
import com.fyp.autologistics.dto.VehicleModelOptionResponse;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.StockAlertRule;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.StockLevelStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.event.ShipmentCreatedEvent;
import com.fyp.autologistics.event.ShipmentStatusChangedEvent;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.StockAlertRuleRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StockMonitoringService {

    private static final String RULE_KEY_PREFIX = "STK-";

    private final StockAlertRuleRepository stockAlertRuleRepository;
    private final ShipmentPartyProfileRepository shipmentPartyProfileRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final ShipmentRepository shipmentRepository;
    private final AlertService alertService;
    private final AuthenticatedUserService authenticatedUserService;
    private final boolean schedulerEnabled;

    public StockMonitoringService(
            StockAlertRuleRepository stockAlertRuleRepository,
            ShipmentPartyProfileRepository shipmentPartyProfileRepository,
            VehicleModelRepository vehicleModelRepository,
            ShipmentRepository shipmentRepository,
            AlertService alertService,
            AuthenticatedUserService authenticatedUserService,
            @Value("${app.stock-monitoring.scheduler-enabled:true}") boolean schedulerEnabled
    ) {
        this.stockAlertRuleRepository = stockAlertRuleRepository;
        this.shipmentPartyProfileRepository = shipmentPartyProfileRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.shipmentRepository = shipmentRepository;
        this.alertService = alertService;
        this.authenticatedUserService = authenticatedUserService;
        this.schedulerEnabled = schedulerEnabled;
    }

    @Transactional(readOnly = true)
    public StockMonitoringOverviewResponse getOverview(String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        validateCanViewStockMonitoring(user);

        List<StockAlertRule> rules = user.getRole() == UserRole.ADMIN
                ? stockAlertRuleRepository
                        .findAllByOrderByActiveDescPartyProfileCompanyNameAscVehicleModelDisplayNameAsc()
                : findRulesForPartyUser(user);

        boolean includeManagementOptions = user.getRole() == UserRole.ADMIN;

        return new StockMonitoringOverviewResponse(
                rules.stream().map(this::mapToResponse).toList(),
                includeManagementOptions ? getPartyOptions() : List.of(),
                includeManagementOptions ? getVehicleModelOptions() : List.of(),
                includeManagementOptions ? getLogisticsStageOptions() : List.of()
        );
    }

    @Transactional
    public StockAlertRuleResponse createRule(StockAlertRuleRequest request, String email) {
        requireAdministrator(email);

        ShipmentPartyProfile partyProfile = findEligibleParty(request.getPartyKey());
        VehicleModel vehicleModel = findActiveVehicleModel(request.getVehicleModelCode());
        validateUniqueScope(partyProfile, vehicleModel, request.getLogisticsStage(), null);

        StockAlertRule rule = new StockAlertRule();
        rule.setRuleKey(generateRuleKey());
        applyRequest(rule, request, partyProfile, vehicleModel);

        StockAlertRule savedRule = stockAlertRuleRepository.save(rule);
        evaluateRule(savedRule);
        return mapToResponse(savedRule);
    }

    @Transactional
    public StockAlertRuleResponse updateRule(
            Long ruleId,
            StockAlertRuleRequest request,
            String email
    ) {
        requireAdministrator(email);

        StockAlertRule rule = findRule(ruleId);
        ShipmentPartyProfile partyProfile = findEligibleParty(request.getPartyKey());
        VehicleModel vehicleModel = findActiveVehicleModel(request.getVehicleModelCode());
        validateUniqueScope(partyProfile, vehicleModel, request.getLogisticsStage(), ruleId);

        applyRequest(rule, request, partyProfile, vehicleModel);
        StockAlertRule savedRule = stockAlertRuleRepository.save(rule);
        evaluateRule(savedRule);
        return mapToResponse(savedRule);
    }

    @Scheduled(fixedDelayString = "${app.stock-monitoring.scheduler-delay-ms:30000}")
    @Transactional
    public void evaluateScheduledRules() {
        if (schedulerEnabled) {
            evaluateAllActiveRules();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void evaluateAfterShipmentCreated(ShipmentCreatedEvent event) {
        if (schedulerEnabled) {
            evaluateAllActiveRules();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void evaluateAfterShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        if (schedulerEnabled) {
            evaluateAllActiveRules();
        }
    }

    void evaluateAllActiveRules() {
        stockAlertRuleRepository
                .findByActiveTrueOrderByPartyProfileCompanyNameAscVehicleModelDisplayNameAsc()
                .forEach(this::evaluateRule);
    }

    private List<StockAlertRule> findRulesForPartyUser(AppUser user) {
        if (!StringUtils.hasText(user.getPartyProfileKey())) {
            return List.of();
        }

        return stockAlertRuleRepository
                .findByPartyProfilePartyKeyOrderByActiveDescVehicleModelDisplayNameAsc(
                        user.getPartyProfileKey()
                );
    }

    private void evaluateRule(StockAlertRule rule) {
        long currentQuantity = countCurrentStock(rule);
        rule.setLastEvaluatedAt(LocalDateTime.now());
        stockAlertRuleRepository.save(rule);
        alertService.synchronizeLowStockAlert(rule, currentQuantity);
    }

    private long countCurrentStock(StockAlertRule rule) {
        String partyKey = rule.getPartyProfile().getPartyKey();
        String modelCode = rule.getVehicleModel().getModelCode();

        return switch (rule.getPartyProfile().getPartyRole()) {
            case MANUFACTURER -> shipmentRepository
                    .countByManufacturerKeyAndVehicleModelCodeAndCurrentStageAndStatusNot(
                            partyKey,
                            modelCode,
                            rule.getLogisticsStage(),
                            ShipmentStatus.CANCELLED
                    );
            case DEALERSHIP -> shipmentRepository
                    .countByDealershipKeyAndVehicleModelCodeAndCurrentStageAndStatusNot(
                            partyKey,
                            modelCode,
                            rule.getLogisticsStage(),
                            ShipmentStatus.CANCELLED
                    );
            default -> throw new BadRequestException(
                    "Stock rules are supported only for manufacturers and dealerships."
            );
        };
    }

    private StockAlertRuleResponse mapToResponse(StockAlertRule rule) {
        long currentQuantity = countCurrentStock(rule);

        return new StockAlertRuleResponse(
                rule.getId(),
                rule.getRuleKey(),
                rule.getPartyProfile().getPartyKey(),
                rule.getPartyProfile().getPartyRole(),
                rule.getPartyProfile().getCompanyName(),
                rule.getVehicleModel().getModelCode(),
                rule.getVehicleModel().getDisplayName(),
                rule.getLogisticsStage(),
                rule.getMinimumQuantity(),
                currentQuantity,
                resolveStockLevelStatus(rule, currentQuantity),
                rule.isActive(),
                rule.getLastEvaluatedAt(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    private StockLevelStatus resolveStockLevelStatus(StockAlertRule rule, long currentQuantity) {
        if (!rule.isActive()) {
            return StockLevelStatus.INACTIVE;
        }

        return currentQuantity < rule.getMinimumQuantity()
                ? StockLevelStatus.LOW
                : StockLevelStatus.HEALTHY;
    }

    private void applyRequest(
            StockAlertRule rule,
            StockAlertRuleRequest request,
            ShipmentPartyProfile partyProfile,
            VehicleModel vehicleModel
    ) {
        rule.setPartyProfile(partyProfile);
        rule.setVehicleModel(vehicleModel);
        rule.setLogisticsStage(request.getLogisticsStage());
        rule.setMinimumQuantity(request.getMinimumQuantity());
        rule.setActive(Boolean.TRUE.equals(request.getActive()));
    }

    private ShipmentPartyProfile findEligibleParty(String partyKey) {
        String normalizedKey = normalizeRequiredKey(partyKey, "Company");
        ShipmentPartyProfile partyProfile = shipmentPartyProfileRepository
                .findByPartyKey(normalizedKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Selected company profile was not found."
                ));

        if (!partyProfile.isActive()) {
            throw new BadRequestException("Selected company profile is inactive.");
        }

        if (partyProfile.getPartyRole() != UserRole.MANUFACTURER
                && partyProfile.getPartyRole() != UserRole.DEALERSHIP) {
            throw new BadRequestException(
                    "Stock rules can only be assigned to manufacturers or dealerships."
            );
        }

        return partyProfile;
    }

    private VehicleModel findActiveVehicleModel(String modelCode) {
        return vehicleModelRepository
                .findByModelCodeAndActiveTrue(normalizeRequiredKey(modelCode, "Vehicle model"))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Selected vehicle model was not found or is inactive."
                ));
    }

    private void validateUniqueScope(
            ShipmentPartyProfile partyProfile,
            VehicleModel vehicleModel,
            LogisticsStage logisticsStage,
            Long existingRuleId
    ) {
        if (logisticsStage == null) {
            throw new BadRequestException("Logistics stage is required.");
        }

        boolean duplicateExists = existingRuleId == null
                ? stockAlertRuleRepository
                        .existsByPartyProfilePartyKeyAndVehicleModelModelCodeAndLogisticsStage(
                                partyProfile.getPartyKey(),
                                vehicleModel.getModelCode(),
                                logisticsStage
                        )
                : stockAlertRuleRepository
                        .existsByPartyProfilePartyKeyAndVehicleModelModelCodeAndLogisticsStageAndIdNot(
                                partyProfile.getPartyKey(),
                                vehicleModel.getModelCode(),
                                logisticsStage,
                                existingRuleId
                        );

        if (duplicateExists) {
            throw new BadRequestException(
                    "A stock rule already exists for this company, vehicle model, and stage."
            );
        }
    }

    private StockAlertRule findRule(Long ruleId) {
        return stockAlertRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock alert rule not found with id: " + ruleId
                ));
    }

    private void requireAdministrator(String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);

        if (user.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Only administrators can manage stock alert rules.");
        }
    }

    private void validateCanViewStockMonitoring(AppUser user) {
        if (user.getRole() == UserRole.ADMIN
                || user.getRole() == UserRole.MANUFACTURER
                || user.getRole() == UserRole.DEALERSHIP) {
            return;
        }

        throw new BadRequestException("You are not allowed to view stock monitoring.");
    }

    private List<StockPartyOptionResponse> getPartyOptions() {
        Comparator<ShipmentPartyProfile> sortOrder = Comparator
                .comparing(ShipmentPartyProfile::getSortOrder)
                .thenComparing(ShipmentPartyProfile::getCompanyName);

        return Stream.concat(
                        shipmentPartyProfileRepository
                                .findByPartyRoleAndActiveTrueOrderBySortOrderAscCompanyNameAsc(
                                        UserRole.MANUFACTURER
                                )
                                .stream(),
                        shipmentPartyProfileRepository
                                .findByPartyRoleAndActiveTrueOrderBySortOrderAscCompanyNameAsc(
                                        UserRole.DEALERSHIP
                                )
                                .stream()
                )
                .sorted(sortOrder)
                .map(profile -> new StockPartyOptionResponse(
                        profile.getPartyKey(),
                        profile.getCompanyName(),
                        profile.getPartyRole()
                ))
                .toList();
    }

    private List<VehicleModelOptionResponse> getVehicleModelOptions() {
        return vehicleModelRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc()
                .stream()
                .map(model -> new VehicleModelOptionResponse(
                        model.getModelCode(),
                        model.getBrand(),
                        model.getDisplayName()
                ))
                .toList();
    }

    private List<MasterDataOptionResponse> getLogisticsStageOptions() {
        return Stream.of(LogisticsStage.values())
                .map(stage -> new MasterDataOptionResponse(stage.name(), formatEnumLabel(stage.name())))
                .toList();
    }

    private String generateRuleKey() {
        String ruleKey;

        do {
            ruleKey = RULE_KEY_PREFIX + UUID.randomUUID().toString().toUpperCase();
        } while (stockAlertRuleRepository.existsByRuleKey(ruleKey));

        return ruleKey;
    }

    private String normalizeRequiredKey(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(label + " is required.");
        }

        return value.trim();
    }

    private String formatEnumLabel(String value) {
        String normalized = value.replace('_', ' ').toLowerCase();
        StringBuilder result = new StringBuilder(normalized.length());
        boolean capitalizeNext = true;

        for (char character : normalized.toCharArray()) {
            result.append(capitalizeNext ? Character.toUpperCase(character) : character);
            capitalizeNext = character == ' ';
        }

        return result.toString();
    }
}
