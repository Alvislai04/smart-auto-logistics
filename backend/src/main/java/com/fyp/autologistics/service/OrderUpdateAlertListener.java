package com.fyp.autologistics.service;

import com.fyp.autologistics.event.ShipmentStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderUpdateAlertListener {

    private final AlertService alertService;

    public OrderUpdateAlertListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void createOrderUpdateAlert(ShipmentStatusChangedEvent event) {
        if (event.previousStatus() == null || event.previousStatus() == event.currentStatus()) {
            return;
        }

        alertService.createOrderUpdateAlert(
                event.shipmentId(),
                event.previousStatus(),
                event.currentStatus(),
                event.sourceType()
        );
    }
}
