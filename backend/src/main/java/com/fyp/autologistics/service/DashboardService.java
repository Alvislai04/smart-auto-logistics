package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.AlertResponse;
import com.fyp.autologistics.dto.DashboardOverviewResponse;
import com.fyp.autologistics.dto.DashboardSummaryResponse;
import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.StockAlertRule;
import com.fyp.autologistics.enums.AlertReferenceType;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.repository.AlertRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.repository.ShipmentRepository.ShipmentStatusCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.PageRequest;

import java.util.EnumMap;
import java.util.List;

@Service
public class DashboardService {

    private final ShipmentRepository shipmentRepository;
    private final AlertRepository alertRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public DashboardService(
            ShipmentRepository shipmentRepository,
            AlertRepository alertRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.alertRepository = alertRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getDashboardOverview(String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);

        return new DashboardOverviewResponse(
                buildDashboardSummary(user),
                mapOpenAlerts(user)
        );
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        return buildDashboardSummary(user);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getDashboardOpenAlerts(String email) {
        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        return mapOpenAlerts(user);
    }

    private DashboardSummaryResponse buildDashboardSummary(AppUser user) {
        List<ShipmentStatusCount> statusCounts = findShipmentStatusCounts(user);
        long totalShipments = statusCounts.stream()
                .mapToLong(this::resolveCountTotal)
                .sum();
        EnumMap<ShipmentStatus, Long> countsByStatus = toCountsByStatus(statusCounts);

        return new DashboardSummaryResponse(
                totalShipments,
                countByStatus(countsByStatus, ShipmentStatus.CREATED),
                countByStatus(countsByStatus, ShipmentStatus.READY_FOR_DISPATCH),
                countByStatus(countsByStatus, ShipmentStatus.PICKED_UP),
                countByStatus(countsByStatus, ShipmentStatus.IN_TRANSIT),
                countByStatus(countsByStatus, ShipmentStatus.DELAYED),
                countByStatus(countsByStatus, ShipmentStatus.ARRIVED),
                countByStatus(countsByStatus, ShipmentStatus.DELIVERED),
                countByStatus(countsByStatus, ShipmentStatus.CANCELLED),
                countOpenAlerts(user)
        );
    }

    private List<AlertResponse> mapOpenAlerts(AppUser user) {
        return findOpenAlerts(user)
                .stream()
                .map(this::mapToAlertResponse)
                .toList();
    }

    private List<ShipmentStatusCount> findShipmentStatusCounts(AppUser user) {
        UserRole role = user.getRole();

        if (role == UserRole.MANUFACTURER) {
            String partyProfileKey = user.getPartyProfileKey();
            if (!StringUtils.hasText(partyProfileKey)) {
                return List.of();
            }

            return shipmentRepository.countShipmentsByManufacturerKeyGrouped(partyProfileKey);
        }

        if (role == UserRole.DEALERSHIP) {
            String partyProfileKey = user.getPartyProfileKey();
            if (!StringUtils.hasText(partyProfileKey)) {
                return List.of();
            }

            return shipmentRepository.countShipmentsByDealershipKeyGrouped(partyProfileKey);
        }

        return shipmentRepository.countShipmentsByStatus();
    }

    private EnumMap<ShipmentStatus, Long> toCountsByStatus(List<ShipmentStatusCount> statusCounts) {
        EnumMap<ShipmentStatus, Long> countsByStatus = new EnumMap<>(ShipmentStatus.class);

        statusCounts.forEach(row -> {
            if (row.getStatus() != null) {
                countsByStatus.put(row.getStatus(), resolveCountTotal(row));
            }
        });

        return countsByStatus;
    }

    private long countByStatus(
            EnumMap<ShipmentStatus, Long> countsByStatus,
            ShipmentStatus status
    ) {
        return countsByStatus.getOrDefault(status, 0L);
    }

    private long resolveCountTotal(ShipmentStatusCount statusCount) {
        return statusCount.getTotal() == null ? 0 : statusCount.getTotal();
    }

    private long countOpenAlerts(AppUser user) {
        UserRole role = user.getRole();

        if (role == UserRole.MANUFACTURER) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.countVisibleToManufacturer(
                            AlertStatus.OPEN,
                            partyProfileKey
                    )
                    : 0;
        }

        if (role == UserRole.DEALERSHIP) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.countVisibleToDealership(
                            AlertStatus.OPEN,
                            partyProfileKey
                    )
                    : 0;
        }

        if (role == UserRole.LOGISTICS_PROVIDER) {
            return alertRepository.countByStatusAndShipmentIsNotNull(AlertStatus.OPEN);
        }

        return alertRepository.countByStatus(AlertStatus.OPEN);
    }

    private List<Alert> findOpenAlerts(AppUser user) {
        UserRole role = user.getRole();

        if (role == UserRole.MANUFACTURER) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.findVisibleToManufacturer(
                            AlertStatus.OPEN,
                            partyProfileKey,
                            PageRequest.of(0, 5)
                    )
                    : List.of();
        }

        if (role == UserRole.DEALERSHIP) {
            String partyProfileKey = user.getPartyProfileKey();
            return StringUtils.hasText(partyProfileKey)
                    ? alertRepository.findVisibleToDealership(
                            AlertStatus.OPEN,
                            partyProfileKey,
                            PageRequest.of(0, 5)
                    )
                    : List.of();
        }

        if (role == UserRole.LOGISTICS_PROVIDER) {
            return alertRepository.findByStatusAndShipmentIsNotNullOrderByCreatedAtDesc(
                    AlertStatus.OPEN,
                    PageRequest.of(0, 5)
            );
        }

        return alertRepository.findTop5ByStatusOrderByCreatedAtDesc(AlertStatus.OPEN);
    }

    private AlertResponse mapToAlertResponse(Alert alert) {
        Shipment shipment = alert.getShipment();
        StockAlertRule stockAlertRule = alert.getStockAlertRule();
        AlertReferenceType referenceType = shipment != null
                ? AlertReferenceType.SHIPMENT
                : AlertReferenceType.STOCK_RULE;

        return new AlertResponse(
                alert.getId(),
                shipment == null ? null : shipment.getId(),
                shipment == null ? null : shipment.getShipmentCode(),
                referenceType,
                shipment != null ? shipment.getId() : stockAlertRule.getId(),
                shipment != null ? shipment.getShipmentCode() : stockAlertRule.getRuleKey(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt()
        );
    }
}
