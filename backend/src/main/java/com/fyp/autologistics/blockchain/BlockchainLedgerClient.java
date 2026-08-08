package com.fyp.autologistics.blockchain;

public interface BlockchainLedgerClient {

    BlockchainTransactionReceipt recordShipmentEvent(
            Long shipmentId,
            Long shipmentEventId,
            String eventHash
    );

    String getRecordedEventHash(Long shipmentEventId);
}
