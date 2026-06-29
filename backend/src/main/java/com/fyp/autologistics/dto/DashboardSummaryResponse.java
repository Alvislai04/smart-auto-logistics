package com.fyp.autologistics.dto;

public class DashboardSummaryResponse {

    private long totalShipments;
    private long createdShipments;
    private long readyForDispatchShipments;
    private long pickedUpShipments;
    private long inTransitShipments;
    private long delayedShipments;
    private long arrivedShipments;
    private long deliveredShipments;
    private long cancelledShipments;
    private long openAlerts;

    public DashboardSummaryResponse(
            long totalShipments,
            long createdShipments,
            long readyForDispatchShipments,
            long pickedUpShipments,
            long inTransitShipments,
            long delayedShipments,
            long arrivedShipments,
            long deliveredShipments,
            long cancelledShipments,
            long openAlerts
    ) {
        this.totalShipments = totalShipments;
        this.createdShipments = createdShipments;
        this.readyForDispatchShipments = readyForDispatchShipments;
        this.pickedUpShipments = pickedUpShipments;
        this.inTransitShipments = inTransitShipments;
        this.delayedShipments = delayedShipments;
        this.arrivedShipments = arrivedShipments;
        this.deliveredShipments = deliveredShipments;
        this.cancelledShipments = cancelledShipments;
        this.openAlerts = openAlerts;
    }

    public long getTotalShipments() {
        return totalShipments;
    }

    public long getCreatedShipments() {
        return createdShipments;
    }

    public long getReadyForDispatchShipments() {
        return readyForDispatchShipments;
    }

    public long getPickedUpShipments() {
        return pickedUpShipments;
    }

    public long getInTransitShipments() {
        return inTransitShipments;
    }

    public long getDelayedShipments() {
        return delayedShipments;
    }

    public long getArrivedShipments() {
        return arrivedShipments;
    }

    public long getDeliveredShipments() {
        return deliveredShipments;
    }

    public long getCancelledShipments() {
        return cancelledShipments;
    }

    public long getOpenAlerts() {
        return openAlerts;
    }
}