package com.fyp.autologistics.blockchain;

public class BlockchainLedgerException extends RuntimeException {

    public BlockchainLedgerException(String message) {
        super(message);
    }

    public BlockchainLedgerException(String message, Throwable cause) {
        super(message, cause);
    }
}
