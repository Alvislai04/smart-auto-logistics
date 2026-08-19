package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.SimulationSessionResponse;
import com.fyp.autologistics.enums.SimulationSessionStatus;
import com.fyp.autologistics.service.SimulationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulation-sessions")
public class SimulationSessionController {

    private final SimulationService simulationService;

    public SimulationSessionController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping
    public List<SimulationSessionResponse> getSimulationSessions(
            @RequestParam(required = false) SimulationSessionStatus status
    ) {
        return simulationService.getSimulationSessions(status);
    }
}
