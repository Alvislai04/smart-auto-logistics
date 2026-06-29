package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);

    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    boolean existsByShipmentIdAndAlertTypeAndStatus(
            Long shipmentId,
            AlertType alertType,
            AlertStatus status
    );

    long countByStatus(AlertStatus status);
}