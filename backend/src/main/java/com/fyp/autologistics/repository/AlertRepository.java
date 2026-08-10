package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    List<Alert> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);

    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<Alert> findTop5ByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<Alert> findByStatusAndShipmentManufacturerKeyOrderByCreatedAtDesc(
            AlertStatus status,
            String manufacturerKey
    );

    List<Alert> findTop5ByStatusAndShipmentManufacturerKeyOrderByCreatedAtDesc(
            AlertStatus status,
            String manufacturerKey
    );

    List<Alert> findByStatusAndShipmentDealershipKeyOrderByCreatedAtDesc(
            AlertStatus status,
            String dealershipKey
    );

    List<Alert> findTop5ByStatusAndShipmentDealershipKeyOrderByCreatedAtDesc(
            AlertStatus status,
            String dealershipKey
    );

    boolean existsByShipmentIdAndAlertTypeAndStatus(
            Long shipmentId,
            AlertType alertType,
            AlertStatus status
    );

    long countByStatus(AlertStatus status);

    long countByStatusAndShipmentManufacturerKey(AlertStatus status, String manufacturerKey);

    long countByStatusAndShipmentDealershipKey(AlertStatus status, String dealershipKey);
}
