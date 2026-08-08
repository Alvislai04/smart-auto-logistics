package com.fyp.autologistics.blockchain;

public record BlockchainTransactionReceipt(
        String transactionHash,
        Long blockNumber
) {
}
