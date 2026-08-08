package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentPartyProfileRepository
        extends JpaRepository<ShipmentPartyProfile, Long> {

    List<ShipmentPartyProfile> findByPartyRoleAndActiveTrueOrderBySortOrderAscCompanyNameAsc(
            UserRole partyRole
    );

    Optional<ShipmentPartyProfile> findByPartyKeyAndPartyRoleAndActiveTrue(
            String partyKey,
            UserRole partyRole
    );
}
