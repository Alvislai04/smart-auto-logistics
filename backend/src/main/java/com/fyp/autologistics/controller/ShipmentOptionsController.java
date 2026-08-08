package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.ShipmentFormOptionsResponse;
import com.fyp.autologistics.service.ShipmentOptionsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipment-options")
@CrossOrigin(origins = "http://localhost:5173")
public class ShipmentOptionsController {

    private final ShipmentOptionsService shipmentOptionsService;

    public ShipmentOptionsController(ShipmentOptionsService shipmentOptionsService) {
        this.shipmentOptionsService = shipmentOptionsService;
    }

    @GetMapping
    public ShipmentFormOptionsResponse getShipmentFormOptions() {
        return shipmentOptionsService.getShipmentFormOptions();
    }
}
