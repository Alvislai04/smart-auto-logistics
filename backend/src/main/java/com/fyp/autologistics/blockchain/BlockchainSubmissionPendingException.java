package com.fyp.autologistics.blockchain;

public class BlockchainSubmissionPendingException extends BlockchainLedgerException {

    private final String transactionHash;

    public BlockchainSubmissionPendingException(
            String message,
            String transactionHash,
            Throwable cause
    ) {
        super(message, cause);
        this.transactionHash = transactionHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }
}
