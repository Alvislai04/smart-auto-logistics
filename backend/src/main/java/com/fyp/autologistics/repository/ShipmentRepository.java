package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface ShipmentRepository
        extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {

    long countByStatus(ShipmentStatus status);

    List<Shipment> findByStatusIn(Collection<ShipmentStatus> statuses);

    boolean existsByShipmentCode(String shipmentCode);
}
