package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.CreateShipmentRequest;
import com.fyp.autologistics.dto.ShipmentResponse;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@CrossOrigin(origins = "http://localhost:5173")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ShipmentResponse createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        return shipmentService.createShipment(request);
    }

    @GetMapping
    public List<ShipmentResponse> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    @GetMapping("/{id}")
    public ShipmentResponse getShipmentById(@PathVariable Long id) {
        return shipmentService.getShipmentById(id);
    }

    @PutMapping("/{id}/status")
    public ShipmentResponse updateShipmentStatus(
            @PathVariable Long id,
            @RequestParam ShipmentStatus status
    ) {
        return shipmentService.updateShipmentStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public String deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return "Shipment deleted successfully";
    }
}