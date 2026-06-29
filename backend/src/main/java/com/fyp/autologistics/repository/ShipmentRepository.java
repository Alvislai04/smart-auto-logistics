package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    long countByStatus(ShipmentStatus status);
}