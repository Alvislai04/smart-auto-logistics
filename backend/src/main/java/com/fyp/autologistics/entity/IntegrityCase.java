package com.fyp.autologistics.entity;

import com.fyp.autologistics.enums.IntegrityCaseResolutionType;
import com.fyp.autologistics.enums.IntegrityCaseStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "integrity_cases")
public class IntegrityCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long blockchainRecordId;

    private Long shipmentId;

    private Long shipmentEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrityCaseStatus status;

    @Enumerated(EnumType.STRING)
    private IntegrityCaseResolutionType resolutionType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(length = 2000)
    private String investigationNotes;

    @Column(length = 2000)
    private String resolutionNotes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    public IntegrityCase() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }

        if (this.status == null) {
            this.status = IntegrityCaseStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getBlockchainRecordId() {
        return blockchainRecordId;
    }

    public void setBlockchainRecordId(Long blockchainRecordId) {
        this.blockchainRecordId = blockchainRecordId;
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

    public IntegrityCaseStatus getStatus() {
        return status;
    }

    public void setStatus(IntegrityCaseStatus status) {
        this.status = status;
    }

    public IntegrityCaseResolutionType getResolutionType() {
        return resolutionType;
    }

    public void setResolutionType(IntegrityCaseResolutionType resolutionType) {
        this.resolutionType = resolutionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvestigationNotes() {
        return investigationNotes;
    }

    public void setInvestigationNotes(String investigationNotes) {
        this.investigationNotes = investigationNotes;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
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

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}