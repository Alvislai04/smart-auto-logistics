package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.SimulationSessionResponse;
import com.fyp.autologistics.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments/{shipmentId}/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/session")
    public ResponseEntity<SimulationSessionResponse> getShipmentSimulationSession(
            @PathVariable Long shipmentId
    ) {
        return simulationService.getShipmentSimulationSession(shipmentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
