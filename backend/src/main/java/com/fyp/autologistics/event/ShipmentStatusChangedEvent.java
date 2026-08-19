package com.fyp.autologistics.event;

import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentStatus;

public record ShipmentStatusChangedEvent(
        Long shipmentId,
        ShipmentStatus previousStatus,
        ShipmentStatus currentStatus,
        ShipmentEventSourceType sourceType
) {
}
