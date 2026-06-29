package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.IntegrityCaseResolutionType;
import com.fyp.autologistics.enums.IntegrityCaseStatus;

import java.time.LocalDateTime;

public class IntegrityCaseResponse {

    private Long id;
    private Long blockchainRecordId;
    private Long shipmentId;
    private Long shipmentEventId;
    private IntegrityCaseStatus status;
    private IntegrityCaseResolutionType resolutionType;
    private String title;
    private String description;
    private String investigationNotes;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public IntegrityCaseResponse(
            Long id,
            Long blockchainRecordId,
            Long shipmentId,
            Long shipmentEventId,
            IntegrityCaseStatus status,
            IntegrityCaseResolutionType resolutionType,
            String title,
            String description,
            String investigationNotes,
            String resolutionNotes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime resolvedAt
    ) {
        this.id = id;
        this.blockchainRecordId = blockchainRecordId;
        this.shipmentId = shipmentId;
        this.shipmentEventId = shipmentEventId;
        this.status = status;
        this.resolutionType = resolutionType;
        this.title = title;
        this.description = description;
        this.investigationNotes = investigationNotes;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getBlockchainRecordId() {
        return blockchainRecordId;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public Long getShipmentEventId() {
        return shipmentEventId;
    }

    public IntegrityCaseStatus getStatus() {
        return status;
    }

    public IntegrityCaseResolutionType getResolutionType() {
        return resolutionType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInvestigationNotes() {
        return investigationNotes;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}