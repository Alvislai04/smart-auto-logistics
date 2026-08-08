package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.ConfirmDeliveryReceiptRequest;
import com.fyp.autologistics.dto.CreateShipmentRequest;
import com.fyp.autologistics.dto.ShipmentPageResponse;
import com.fyp.autologistics.dto.ShipmentResponse;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    public ShipmentPageResponse searchShipments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) LogisticsStage stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return shipmentService.searchShipments(
                search,
                status,
                stage,
                page,
                size,
                sortBy,
                sortDirection
        );
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

    @PutMapping("/{id}/receipt-confirmation")
    public ShipmentResponse confirmDeliveryReceipt(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmDeliveryReceiptRequest request
    ) {
        return shipmentService.confirmDeliveryReceipt(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return "Shipment deleted successfully";
    }
}
