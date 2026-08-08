package com.fyp.autologistics.dto;

import jakarta.validation.constraints.NotBlank;

public class ConfirmDeliveryReceiptRequest {

    @NotBlank(message = "Confirmed by is required")
    private String confirmedBy;

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }
}
