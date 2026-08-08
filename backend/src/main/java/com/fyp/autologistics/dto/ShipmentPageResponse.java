package com.fyp.autologistics.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class ShipmentPageResponse {

    private final List<ShipmentResponse> content;
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;
    private final int numberOfElements;
    private final boolean first;
    private final boolean last;

    public ShipmentPageResponse(
            List<ShipmentResponse> content,
            int page,
            int size,
            int totalPages,
            long totalElements,
            int numberOfElements,
            boolean first,
            boolean last
    ) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.numberOfElements = numberOfElements;
        this.first = first;
        this.last = last;
    }

    public static ShipmentPageResponse from(Page<ShipmentResponse> shipmentPage) {
        return new ShipmentPageResponse(
                shipmentPage.getContent(),
                shipmentPage.getNumber(),
                shipmentPage.getSize(),
                shipmentPage.getTotalPages(),
                shipmentPage.getTotalElements(),
                shipmentPage.getNumberOfElements(),
                shipmentPage.isFirst(),
                shipmentPage.isLast()
        );
    }

    public List<ShipmentResponse> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}
