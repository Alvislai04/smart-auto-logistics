package com.fyp.autologistics.blockchain;

import java.util.Optional;

public interface BlockchainLedgerClient {

    BlockchainTransactionReceipt recordShipmentEvent(
            Long shipmentId,
            Long shipmentEventId,
            String eventHash
    );

    String getRecordedEventHash(Long shipmentEventId);

    Optional<BlockchainTransactionReceipt> getTransactionReceipt(String transactionHash);

    BlockchainHealthSnapshot getHealthSnapshot();
}
