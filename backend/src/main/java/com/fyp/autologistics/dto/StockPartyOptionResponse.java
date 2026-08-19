package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.UserRole;

public record StockPartyOptionResponse(
        String key,
        String name,
        UserRole role
) {
}
