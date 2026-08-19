package com.fyp.autologistics.blockchain;

import com.fyp.autologistics.enums.BlockchainHealthStatus;

public record BlockchainHealthSnapshot(
        BlockchainHealthStatus status,
        boolean enabled,
        boolean connected,
        boolean contractConfigured,
        boolean contractDeployed,
        boolean signerConfigured,
        String contractAddress,
        Long chainId,
        Long latestBlockNumber,
        String message
) {
}
