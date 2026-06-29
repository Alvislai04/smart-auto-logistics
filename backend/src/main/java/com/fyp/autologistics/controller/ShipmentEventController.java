package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.ShipmentEventResponse;
import com.fyp.autologistics.service.ShipmentEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments/{shipmentId}/events")
@CrossOrigin(origins = "http://localhost:5173")
public class ShipmentEventController {

    private final ShipmentEventService shipmentEventService;

    public ShipmentEventController(ShipmentEventService shipmentEventService) {
        this.shipmentEventService = shipmentEventService;
    }

    @GetMapping
    public List<ShipmentEventResponse> getEventsByShipmentId(@PathVariable Long shipmentId) {
        return shipmentEventService.getEventsByShipmentId(shipmentId);
    }
}