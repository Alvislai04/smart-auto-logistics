package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.SimulationOptionsResponse;
import com.fyp.autologistics.dto.SimulationSessionResponse;
import com.fyp.autologistics.enums.SimulationSessionStatus;
import com.fyp.autologistics.service.SimulationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulation-sessions")
@CrossOrigin(origins = "http://localhost:5173")
public class SimulationSessionController {

    private final SimulationService simulationService;

    public SimulationSessionController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/options")
    public SimulationOptionsResponse getSimulationOptions() {
        return simulationService.getSimulationOptions();
    }

    @GetMapping
    public List<SimulationSessionResponse> getSimulationSessions(
            @RequestParam(required = false) SimulationSessionStatus status
    ) {
        return simulationService.getSimulationSessions(status);
    }

    @PutMapping("/{sessionId}/pause")
    public SimulationSessionResponse pauseSimulationSession(@PathVariable Long sessionId) {
        return simulationService.pauseSimulationSession(sessionId);
    }

    @PutMapping("/{sessionId}/resume")
    public SimulationSessionResponse resumeSimulationSession(@PathVariable Long sessionId) {
        return simulationService.resumeSimulationSession(sessionId);
    }

    @PutMapping("/{sessionId}/stop")
    public SimulationSessionResponse stopSimulationSession(@PathVariable Long sessionId) {
        return simulationService.stopSimulationSession(sessionId);
    }
}
