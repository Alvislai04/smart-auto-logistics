package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.BlockchainRecordStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_records")
public class BlockchainRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shipmentId;

    @Column(nullable = false)
    private Long shipmentEventId;

    @Column(nullable = false, length = 1000)
    private String eventHash;

    private String transactionHash;

    private Long blockNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockchainRecordStatus status;

    private LocalDateTime recordedAt;

    private LocalDateTime verifiedAt;

    @Column(length = 1000)
    private String verificationMessage;

    @Column
    private Integer retryCount = 0;

    private LocalDateTime lastRetryAt;

    public BlockchainRecord() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = BlockchainRecordStatus.PENDING;
        }

        if (this.retryCount == null) {
            this.retryCount = 0;
        }

    }

    public Long getId() {
        return id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getShipmentEventId() {
        return shipmentEventId;
    }

    public void setShipmentEventId(Long shipmentEventId) {
        this.shipmentEventId = shipmentEventId;
    }

    public String getEventHash() {
        return eventHash;
    }

    public void setEventHash(String eventHash) {
        this.eventHash = eventHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BlockchainRecordStatus getStatus() {
        return status;
    }

    public void setStatus(BlockchainRecordStatus status) {
        this.status = status;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerificationMessage() {
        return verificationMessage;
    }

    public void setVerificationMessage(String verificationMessage) {
        this.verificationMessage = verificationMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }

    public void setLastRetryAt(LocalDateTime lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }

}
