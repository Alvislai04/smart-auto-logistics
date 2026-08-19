package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.StockAlertRuleRequest;
import com.fyp.autologistics.dto.StockAlertRuleResponse;
import com.fyp.autologistics.dto.StockMonitoringOverviewResponse;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.StockAlertRule;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.StockLevelStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.StockAlertRuleRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StockMonitoringServiceTests {

    private final StockAlertRuleRepository stockAlertRuleRepository =
            mock(StockAlertRuleRepository.class);
    private final ShipmentPartyProfileRepository shipmentPartyProfileRepository =
            mock(ShipmentPartyProfileRepository.class);
    private final VehicleModelRepository vehicleModelRepository =
            mock(VehicleModelRepository.class);
    private final ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
    private final AlertService alertService = mock(AlertService.class);
    private final AuthenticatedUserService authenticatedUserService =
            mock(AuthenticatedUserService.class);

    private final StockMonitoringService stockMonitoringService = new StockMonitoringService(
            stockAlertRuleRepository,
            shipmentPartyProfileRepository,
            vehicleModelRepository,
            shipmentRepository,
            alertService,
            authenticatedUserService,
            true
    );

    @Test
    void manufacturerSeesOnlyOwnRulesWithCalculatedStockLevel() {
        AppUser manufacturer = buildUser(UserRole.MANUFACTURER, "MFG-PEKAN");
        StockAlertRule rule = buildRule();

        when(authenticatedUserService.getAuthenticatedUser("manufacturer@example.com"))
                .thenReturn(manufacturer);
        when(stockAlertRuleRepository
                .findByPartyProfilePartyKeyOrderByActiveDescVehicleModelDisplayNameAsc(
                        "MFG-PEKAN"
                )).thenReturn(List.of(rule));
        when(shipmentRepository
                .countByManufacturerKeyAndVehicleModelCodeAndCurrentStageAndStatusNot(
                        "MFG-PEKAN",
                        "MODEL-001",
                        LogisticsStage.STOCK_YARD,
                        ShipmentStatus.CANCELLED
                )).thenReturn(2L);

        StockMonitoringOverviewResponse response = stockMonitoringService.getOverview(
                "manufacturer@example.com"
        );

        assertThat(response.rules()).hasSize(1);
        assertThat(response.rules().getFirst().currentQuantity()).isEqualTo(2);
        assertThat(response.rules().getFirst().stockLevelStatus())
                .isEqualTo(StockLevelStatus.LOW);
        assertThat(response.partyOptions()).isEmpty();
    }

    @Test
    void adminCreationUsesDatabaseProfilesAndEvaluatesImmediately() {
        AppUser admin = buildUser(UserRole.ADMIN, null);
        StockAlertRuleRequest request = buildRequest();
        ShipmentPartyProfile partyProfile = buildPartyProfile();
        VehicleModel vehicleModel = buildVehicleModel();

        when(authenticatedUserService.getAuthenticatedUser("admin@example.com"))
                .thenReturn(admin);
        when(shipmentPartyProfileRepository.findByPartyKey("MFG-PEKAN"))
                .thenReturn(Optional.of(partyProfile));
        when(vehicleModelRepository.findByModelCodeAndActiveTrue("MODEL-001"))
                .thenReturn(Optional.of(vehicleModel));
        when(stockAlertRuleRepository
                .existsByPartyProfilePartyKeyAndVehicleModelModelCodeAndLogisticsStage(
                        "MFG-PEKAN",
                        "MODEL-001",
                        LogisticsStage.STOCK_YARD
                )).thenReturn(false);
        when(stockAlertRuleRepository.existsByRuleKey(any(String.class))).thenReturn(false);
        when(stockAlertRuleRepository.save(any(StockAlertRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentRepository
                .countByManufacturerKeyAndVehicleModelCodeAndCurrentStageAndStatusNot(
                        "MFG-PEKAN",
                        "MODEL-001",
                        LogisticsStage.STOCK_YARD,
                        ShipmentStatus.CANCELLED
                )).thenReturn(2L);

        StockAlertRuleResponse response = stockMonitoringService.createRule(
                request,
                "admin@example.com"
        );

        assertThat(response.ruleKey()).startsWith("STK-");
        assertThat(response.currentQuantity()).isEqualTo(2);
        verify(alertService).synchronizeLowStockAlert(any(StockAlertRule.class), eq(2L));
    }

    @Test
    void duplicateCompanyModelAndStageRuleIsRejected() {
        AppUser admin = buildUser(UserRole.ADMIN, null);
        StockAlertRuleRequest request = buildRequest();

        when(authenticatedUserService.getAuthenticatedUser("admin@example.com"))
                .thenReturn(admin);
        when(shipmentPartyProfileRepository.findByPartyKey("MFG-PEKAN"))
                .thenReturn(Optional.of(buildPartyProfile()));
        when(vehicleModelRepository.findByModelCodeAndActiveTrue("MODEL-001"))
                .thenReturn(Optional.of(buildVehicleModel()));
        when(stockAlertRuleRepository
                .existsByPartyProfilePartyKeyAndVehicleModelModelCodeAndLogisticsStage(
                        "MFG-PEKAN",
                        "MODEL-001",
                        LogisticsStage.STOCK_YARD
                )).thenReturn(true);

        assertThatThrownBy(() -> stockMonitoringService.createRule(request, "admin@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(stockAlertRuleRepository, never()).save(any(StockAlertRule.class));
        verify(alertService, never()).synchronizeLowStockAlert(any(), anyLong());
    }

    private AppUser buildUser(UserRole role, String partyKey) {
        AppUser user = new AppUser();
        user.setRole(role);
        user.setPartyProfileKey(partyKey);
        return user;
    }

    private StockAlertRuleRequest buildRequest() {
        StockAlertRuleRequest request = new StockAlertRuleRequest();
        request.setPartyKey("MFG-PEKAN");
        request.setVehicleModelCode("MODEL-001");
        request.setLogisticsStage(LogisticsStage.STOCK_YARD);
        request.setMinimumQuantity(3);
        request.setActive(true);
        return request;
    }

    private StockAlertRule buildRule() {
        StockAlertRule rule = new StockAlertRule();
        rule.setRuleKey("STK-001");
        rule.setPartyProfile(buildPartyProfile());
        rule.setVehicleModel(buildVehicleModel());
        rule.setLogisticsStage(LogisticsStage.STOCK_YARD);
        rule.setMinimumQuantity(3);
        rule.setActive(true);
        return rule;
    }

    private ShipmentPartyProfile buildPartyProfile() {
        ShipmentPartyProfile profile = new ShipmentPartyProfile();
        profile.setPartyKey("MFG-PEKAN");
        profile.setPartyRole(UserRole.MANUFACTURER);
        profile.setCompanyName("Pekan Manufacturing");
        profile.setActive(true);
        return profile;
    }

    private VehicleModel buildVehicleModel() {
        VehicleModel model = new VehicleModel();
        model.setModelCode("MODEL-001");
        model.setDisplayName("C-Class");
        model.setActive(true);
        return model;
    }
}
