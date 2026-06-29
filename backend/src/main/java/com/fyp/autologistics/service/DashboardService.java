package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.DashboardSummaryResponse;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.repository.AlertRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final ShipmentRepository shipmentRepository;
    private final AlertRepository alertRepository;

    public DashboardService(
            ShipmentRepository shipmentRepository,
            AlertRepository alertRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        return new DashboardSummaryResponse(
                shipmentRepository.count(),
                shipmentRepository.countByStatus(ShipmentStatus.CREATED),
                shipmentRepository.countByStatus(ShipmentStatus.READY_FOR_DISPATCH),
                shipmentRepository.countByStatus(ShipmentStatus.PICKED_UP),
                shipmentRepository.countByStatus(ShipmentStatus.IN_TRANSIT),
                shipmentRepository.countByStatus(ShipmentStatus.DELAYED),
                shipmentRepository.countByStatus(ShipmentStatus.ARRIVED),
                shipmentRepository.countByStatus(ShipmentStatus.DELIVERED),
                shipmentRepository.countByStatus(ShipmentStatus.CANCELLED),
                alertRepository.countByStatus(AlertStatus.OPEN)
        );
    }
}