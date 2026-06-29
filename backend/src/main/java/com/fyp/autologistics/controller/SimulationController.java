package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.SimulateTrackingRequest;
import com.fyp.autologistics.dto.TrackingEventResponse;
import com.fyp.autologistics.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments/{shipmentId}/simulation")
@CrossOrigin(origins = "http://localhost:5173")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/next-point")
    public TrackingEventResponse simulateNextTrackingPoint(
            @PathVariable Long shipmentId,
            @Valid @RequestBody SimulateTrackingRequest request
    ) {
        return simulationService.simulateNextTrackingPoint(shipmentId, request);
    }
}