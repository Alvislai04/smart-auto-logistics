package com.fyp.autologistics.blockchain;

public class BlockchainTransactionFailedException extends BlockchainLedgerException {

    private final String transactionHash;
    private final Long blockNumber;

    public BlockchainTransactionFailedException(
            String message,
            String transactionHash,
            Long blockNumber
    ) {
        super(message);
        this.transactionHash = transactionHash;
        this.blockNumber = blockNumber;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }
}
