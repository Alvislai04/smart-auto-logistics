package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.StockAlertRule;
import com.fyp.autologistics.enums.LogisticsStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockAlertRuleRepository extends JpaRepository<StockAlertRule, Long> {

    boolean existsByRuleKey(String ruleKey);

    List<StockAlertRule> findAllByOrderByActiveDescPartyProfileCompanyNameAscVehicleModelDisplayNameAsc();

    List<StockAlertRule> findByPartyProfilePartyKeyOrderByActiveDescVehicleModelDisplayNameAsc(
            String partyKey
    );

    List<StockAlertRule> findByActiveTrueOrderByPartyProfileCompanyNameAscVehicleModelDisplayNameAsc();

    boolean existsByPartyProfilePartyKeyAndVehicleModelModelCodeAndLogisticsStage(
            String partyKey,
            String vehicleModelCode,
            LogisticsStage logisticsStage
    );

    boolean existsByPartyProfilePartyKeyAndVehicleModelModelCodeAndLogisticsStageAndIdNot(
            String partyKey,
            String vehicleModelCode,
            LogisticsStage logisticsStage,
            Long id
    );
}
