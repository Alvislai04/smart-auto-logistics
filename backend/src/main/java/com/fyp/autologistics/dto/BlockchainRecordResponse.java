package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.BlockchainRecordStatus;

import java.time.LocalDateTime;

public class BlockchainRecordResponse {

    private Long id;
    private Long shipmentId;
    private Long shipmentEventId;
    private String eventHash;
    private String transactionHash;
    private Long blockNumber;
    private BlockchainRecordStatus status;
    private LocalDateTime recordedAt;
    private LocalDateTime verifiedAt;
    private String verificationMessage;

    public BlockchainRecordResponse(
            Long id,
            Long shipmentId,
            Long shipmentEventId,
            String eventHash,
            String transactionHash,
            Long blockNumber,
            BlockchainRecordStatus status,
            LocalDateTime recordedAt,
            LocalDateTime verifiedAt,
            String verificationMessage
    ) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.shipmentEventId = shipmentEventId;
        this.eventHash = eventHash;
        this.transactionHash = transactionHash;
        this.blockNumber = blockNumber;
        this.status = status;
        this.recordedAt = recordedAt;
        this.verifiedAt = verifiedAt;
        this.verificationMessage = verificationMessage;
    }

    public Long getId() {
        return id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public Long getShipmentEventId() {
        return shipmentEventId;
    }

    public String getEventHash() {
        return eventHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public BlockchainRecordStatus getStatus() {
        return status;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public String getVerificationMessage() {
        return verificationMessage;
    }
}