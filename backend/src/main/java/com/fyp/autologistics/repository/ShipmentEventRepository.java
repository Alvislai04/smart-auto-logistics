package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.ShipmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, Long> {

    List<ShipmentEvent> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
}