package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.BlockchainHealthStatus;

import java.time.LocalDateTime;

public record BlockchainHealthResponse(
        BlockchainHealthStatus status,
        boolean enabled,
        boolean connected,
        boolean contractConfigured,
        boolean contractDeployed,
        boolean signerConfigured,
        String contractAddress,
        Long chainId,
        Long latestBlockNumber,
        String message,
        LocalDateTime checkedAt
) {
}
