package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    List<Alert> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);

    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<Alert> findTop5ByStatusOrderByCreatedAtDesc(AlertStatus status);

    @Query("""
            select a
            from Alert a
            left join a.shipment s
            left join a.stockAlertRule r
            left join r.partyProfile p
            where a.status = :status
              and (s.manufacturerKey = :partyKey or p.partyKey = :partyKey)
            order by a.createdAt desc
            """)
    List<Alert> findVisibleToManufacturer(
            @Param("status") AlertStatus status,
            @Param("partyKey") String partyKey,
            Pageable pageable
    );

    @Query("""
            select a
            from Alert a
            left join a.shipment s
            left join a.stockAlertRule r
            left join r.partyProfile p
            where a.status = :status
              and (s.dealershipKey = :partyKey or p.partyKey = :partyKey)
            order by a.createdAt desc
            """)
    List<Alert> findVisibleToDealership(
            @Param("status") AlertStatus status,
            @Param("partyKey") String partyKey,
            Pageable pageable
    );

    List<Alert> findByStatusAndShipmentIsNotNullOrderByCreatedAtDesc(
            AlertStatus status,
            Pageable pageable
    );

    boolean existsByShipmentIdAndAlertTypeAndStatus(
            Long shipmentId,
            AlertType alertType,
            AlertStatus status
    );

    List<Alert> findByShipmentIdAndAlertTypeAndStatusInOrderByCreatedAtDesc(
            Long shipmentId,
            AlertType alertType,
            Collection<AlertStatus> statuses
    );

    List<Alert> findByStockAlertRuleIdAndAlertTypeAndStatusInOrderByCreatedAtDesc(
            Long stockAlertRuleId,
            AlertType alertType,
            Collection<AlertStatus> statuses
    );

    long countByStatus(AlertStatus status);

    @Query("""
            select count(a)
            from Alert a
            left join a.shipment s
            left join a.stockAlertRule r
            left join r.partyProfile p
            where a.status = :status
              and (s.manufacturerKey = :partyKey or p.partyKey = :partyKey)
            """)
    long countVisibleToManufacturer(
            @Param("status") AlertStatus status,
            @Param("partyKey") String partyKey
    );

    @Query("""
            select count(a)
            from Alert a
            left join a.shipment s
            left join a.stockAlertRule r
            left join r.partyProfile p
            where a.status = :status
              and (s.dealershipKey = :partyKey or p.partyKey = :partyKey)
            """)
    long countVisibleToDealership(
            @Param("status") AlertStatus status,
            @Param("partyKey") String partyKey
    );

    long countByStatusAndShipmentIsNotNull(AlertStatus status);
}
