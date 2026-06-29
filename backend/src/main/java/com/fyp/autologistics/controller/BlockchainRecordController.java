package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.BlockchainRecordResponse;
import com.fyp.autologistics.dto.RecordBlockchainEventRequest;
import com.fyp.autologistics.service.BlockchainRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blockchain-records")
@CrossOrigin(origins = "http://localhost:5173")
public class BlockchainRecordController {

    private final BlockchainRecordService blockchainRecordService;

    public BlockchainRecordController(BlockchainRecordService blockchainRecordService) {
        this.blockchainRecordService = blockchainRecordService;
    }

    @PostMapping
    public BlockchainRecordResponse recordShipmentEvent(
            @Valid @RequestBody RecordBlockchainEventRequest request
    ) {
        return blockchainRecordService.recordShipmentEvent(
                request.getShipmentId(),
                request.getShipmentEventId(),
                request.getEventPayload()
        );
    }

    @GetMapping
    public List<BlockchainRecordResponse> getAllRecords() {
        return blockchainRecordService.getAllRecords();
    }

    @PostMapping("/{recordId}/verify")
    public BlockchainRecordResponse verifyRecord(@PathVariable Long recordId) {
        return blockchainRecordService.verifyRecord(recordId);
    }

    @GetMapping("/shipment/{shipmentId}")
    public List<BlockchainRecordResponse> getRecordsByShipmentId(@PathVariable Long shipmentId) {
        return blockchainRecordService.getRecordsByShipmentId(shipmentId);
    }
}