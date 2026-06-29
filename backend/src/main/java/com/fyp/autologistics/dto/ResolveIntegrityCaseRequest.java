package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.IntegrityCaseResolutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResolveIntegrityCaseRequest {

    @NotNull(message = "Resolution type is required")
    private IntegrityCaseResolutionType resolutionType;

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;

    public IntegrityCaseResolutionType getResolutionType() {
        return resolutionType;
    }

    public void setResolutionType(IntegrityCaseResolutionType resolutionType) {
        this.resolutionType = resolutionType;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}