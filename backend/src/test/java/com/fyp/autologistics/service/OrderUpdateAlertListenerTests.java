package com.fyp.autologistics.service;

import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.event.ShipmentStatusChangedEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OrderUpdateAlertListenerTests {

    private final AlertService alertService = mock(AlertService.class);
    private final OrderUpdateAlertListener listener = new OrderUpdateAlertListener(alertService);

    @Test
    void createsAlertForCommittedStatusTransition() {
        ShipmentStatusChangedEvent event = new ShipmentStatusChangedEvent(
                42L,
                ShipmentStatus.PICKED_UP,
                ShipmentStatus.IN_TRANSIT,
                ShipmentEventSourceType.IOT_SIMULATION
        );

        listener.createOrderUpdateAlert(event);

        verify(alertService).createOrderUpdateAlert(
                42L,
                ShipmentStatus.PICKED_UP,
                ShipmentStatus.IN_TRANSIT,
                ShipmentEventSourceType.IOT_SIMULATION
        );
    }

    @Test
    void ignoresInitialOrUnchangedStatus() {
        listener.createOrderUpdateAlert(new ShipmentStatusChangedEvent(
                42L,
                null,
                ShipmentStatus.CREATED,
                ShipmentEventSourceType.SYSTEM_AUTOMATION
        ));
        listener.createOrderUpdateAlert(new ShipmentStatusChangedEvent(
                42L,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.IN_TRANSIT,
                ShipmentEventSourceType.SYSTEM_AUTOMATION
        ));

        verify(alertService, never()).createOrderUpdateAlert(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
