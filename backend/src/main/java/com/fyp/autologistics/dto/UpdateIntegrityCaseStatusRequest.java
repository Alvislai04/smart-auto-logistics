package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.IntegrityCaseStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateIntegrityCaseStatusRequest {

    @NotNull(message = "Status is required")
    private IntegrityCaseStatus status;

    private String investigationNotes;

    public IntegrityCaseStatus getStatus() {
        return status;
    }

    public void setStatus(IntegrityCaseStatus status) {
        this.status = status;
    }

    public String getInvestigationNotes() {
        return investigationNotes;
    }

    public void setInvestigationNotes(String investigationNotes) {
        this.investigationNotes = investigationNotes;
    }
}