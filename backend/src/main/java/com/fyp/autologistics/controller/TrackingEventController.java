package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.CreateTrackingEventRequest;
import com.fyp.autologistics.dto.TrackingEventResponse;
import com.fyp.autologistics.service.TrackingEventService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments/{shipmentId}/tracking-events")
public class TrackingEventController {

    private final TrackingEventService trackingEventService;

    public TrackingEventController(TrackingEventService trackingEventService) {
        this.trackingEventService = trackingEventService;
    }

    @PostMapping
    public TrackingEventResponse createTrackingEvent(
            @PathVariable Long shipmentId,
            @Valid @RequestBody CreateTrackingEventRequest request,
            Authentication authentication
    ) {
        return trackingEventService.createTrackingEvent(
                shipmentId,
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<TrackingEventResponse> getTrackingEventsByShipmentId(@PathVariable Long shipmentId) {
        return trackingEventService.getTrackingEventsByShipmentId(shipmentId);
    }

    @GetMapping("/latest")
    public TrackingEventResponse getLatestTrackingEventByShipmentId(@PathVariable Long shipmentId) {
        return trackingEventService.getLatestTrackingEventByShipmentId(shipmentId);
    }
}
