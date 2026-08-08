package com.fyp.autologistics.dto;

public record ShipmentPartyOptionResponse(
        String key,
        String name,
        String location,
        Double latitude,
        Double longitude
) {
}
