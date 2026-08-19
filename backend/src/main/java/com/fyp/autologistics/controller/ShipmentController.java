package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.ConfirmDeliveryReceiptRequest;
import com.fyp.autologistics.dto.CreateShipmentRequest;
import com.fyp.autologistics.dto.ShipmentPageResponse;
import com.fyp.autologistics.dto.ShipmentResponse;
import com.fyp.autologistics.dto.UpdateShipmentRequest;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ShipmentResponse createShipment(
            @Valid @RequestBody CreateShipmentRequest request,
            Authentication authentication
    ) {
        return shipmentService.createShipment(request, authentication.getName());
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

    @PutMapping("/{id}")
    public ShipmentResponse updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShipmentRequest request,
            Authentication authentication
    ) {
        return shipmentService.updateShipment(id, request, authentication.getName());
    }

    @PutMapping("/{id}/status")
    public ShipmentResponse updateShipmentStatus(
            @PathVariable Long id,
            @RequestParam ShipmentStatus status,
            Authentication authentication
    ) {
        return shipmentService.updateShipmentStatus(id, status, authentication.getName());
    }

    @PutMapping("/{id}/receipt-confirmation")
    public ShipmentResponse confirmDeliveryReceipt(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmDeliveryReceiptRequest request,
            Authentication authentication
    ) {
        return shipmentService.confirmDeliveryReceipt(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public String deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return "Shipment deleted successfully";
    }
}
