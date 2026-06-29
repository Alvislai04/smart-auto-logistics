package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.BlockchainRecordResponse;
import com.fyp.autologistics.entity.BlockchainRecord;
import com.fyp.autologistics.enums.BlockchainRecordStatus;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.repository.BlockchainRecordRepository;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.enums.AdminNotificationType;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import com.fyp.autologistics.enums.CompanyNotificationType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class BlockchainRecordService {

    private final BlockchainRecordRepository blockchainRecordRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final AdminNotificationService adminNotificationService;
    private final CompanyNotificationService companyNotificationService;
    private final IntegrityCaseService integrityCaseService;

    public BlockchainRecordService(
        BlockchainRecordRepository blockchainRecordRepository,
        ShipmentEventRepository shipmentEventRepository,
        AdminNotificationService adminNotificationService,
        CompanyNotificationService companyNotificationService,
        IntegrityCaseService integrityCaseService
    ) {
        this.blockchainRecordRepository = blockchainRecordRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.adminNotificationService = adminNotificationService;
        this.companyNotificationService = companyNotificationService;
        this.integrityCaseService = integrityCaseService;
    }

    @Transactional
    public BlockchainRecordResponse recordShipmentEvent(
            Long shipmentId,
            Long shipmentEventId,
            String eventPayload
    ) {
        if (blockchainRecordRepository.existsByShipmentEventId(shipmentEventId)) {
            throw new BadRequestException("This shipment event is already recorded on blockchain.");
        }

        String eventHash = generateSha256Hash(eventPayload);

        BlockchainRecord blockchainRecord = new BlockchainRecord();
        blockchainRecord.setShipmentId(shipmentId);
        blockchainRecord.setShipmentEventId(shipmentEventId);
        blockchainRecord.setEventHash(eventHash);
        blockchainRecord.setTransactionHash(generateMockTransactionHash(eventHash));
        blockchainRecord.setBlockNumber(generateMockBlockNumber());
        blockchainRecord.setStatus(BlockchainRecordStatus.RECORDED);

        BlockchainRecord savedRecord = blockchainRecordRepository.save(blockchainRecord);
        return mapToResponse(savedRecord);
    }

    @Transactional
    public BlockchainRecordResponse recordShipmentEventAutomatically(ShipmentEvent shipmentEvent) {
        Optional<BlockchainRecord> existingRecord =
                blockchainRecordRepository.findByShipmentEventId(shipmentEvent.getId());

        if (existingRecord.isPresent()) {
            return mapToResponse(existingRecord.get());
        }

        String eventPayload = buildEventPayload(shipmentEvent);
        String eventHash = generateSha256Hash(eventPayload);

        BlockchainRecord blockchainRecord = new BlockchainRecord();
        blockchainRecord.setShipmentId(shipmentEvent.getShipment().getId());
        blockchainRecord.setShipmentEventId(shipmentEvent.getId());
        blockchainRecord.setEventHash(eventHash);
        blockchainRecord.setTransactionHash(generateMockTransactionHash(eventHash));
        blockchainRecord.setBlockNumber(generateMockBlockNumber());
        blockchainRecord.setStatus(BlockchainRecordStatus.RECORDED);

        BlockchainRecord savedRecord = blockchainRecordRepository.save(blockchainRecord);
        return mapToResponse(savedRecord);
    }

    @Transactional(readOnly = true)
    public List<BlockchainRecordResponse> getAllRecords() {
        return blockchainRecordRepository.findAllByOrderByRecordedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BlockchainRecordResponse> getRecordsByShipmentId(Long shipmentId) {
        return blockchainRecordRepository.findByShipmentIdOrderByRecordedAtDesc(shipmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BlockchainRecordResponse verifyRecord(Long blockchainRecordId) {
        BlockchainRecord blockchainRecord = blockchainRecordRepository.findById(blockchainRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blockchain record not found with id: " + blockchainRecordId
                ));

        ShipmentEvent shipmentEvent = shipmentEventRepository.findById(
                blockchainRecord.getShipmentEventId()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Shipment event not found with id: " + blockchainRecord.getShipmentEventId()
        ));

        String recalculatedPayload = buildEventPayload(shipmentEvent);
        String recalculatedHash = generateSha256Hash(recalculatedPayload);

        boolean hashMatches = blockchainRecord.getEventHash().equals(recalculatedHash);

        blockchainRecord.setVerifiedAt(LocalDateTime.now());

        if (hashMatches) {
            blockchainRecord.setStatus(BlockchainRecordStatus.RECORDED);
            blockchainRecord.setVerificationMessage("Blockchain record verified successfully.");
        } else {
            blockchainRecord.setStatus(BlockchainRecordStatus.FAILED);
            blockchainRecord.setVerificationMessage(
                    "Hash mismatch detected. Shipment event may have been modified."
            );

            adminNotificationService.createUnreadNotificationIfNotExists(
                    AdminNotificationType.BLOCKCHAIN_TAMPER_DETECTED,
                    "Blockchain integrity issue detected",
                    "A hash mismatch was detected for blockchain record #"
                            + blockchainRecord.getId()
                            + ". Shipment event #"
                            + blockchainRecord.getShipmentEventId()
                            + " may have been modified.",
                    "BLOCKCHAIN_RECORD",
                    blockchainRecord.getId()
            );

            companyNotificationService.createCompanyNotificationIfNotExists(
                CompanyNotificationType.BLOCKCHAIN_TAMPER_ALERT,
                "Company Management",
                "Shipment blockchain integrity issue detected",
                "A blockchain verification failure was detected for shipment #"
                        + blockchainRecord.getShipmentId()
                        + ". Blockchain record #"
                        + blockchainRecord.getId()
                        + " does not match shipment event #"
                        + blockchainRecord.getShipmentEventId()
                        + ". Please review this shipment immediately.",
                "BLOCKCHAIN_RECORD",
                blockchainRecord.getId()
            );

            integrityCaseService.createCaseIfNotExists(blockchainRecord);
        }

        BlockchainRecord savedRecord = blockchainRecordRepository.save(blockchainRecord);
        return mapToResponse(savedRecord);
    }

    private String buildEventPayload(ShipmentEvent shipmentEvent) {
        return String.join(
                "|",
                shipmentEvent.getShipment().getId().toString(),
                shipmentEvent.getShipment().getShipmentCode(),
                shipmentEvent.getId().toString(),
                shipmentEvent.getEventType().name(),
                shipmentEvent.getDescription(),
                shipmentEvent.getPreviousStatus() == null ? "" : shipmentEvent.getPreviousStatus(),
                shipmentEvent.getNewStatus() == null ? "" : shipmentEvent.getNewStatus(),
                shipmentEvent.getCreatedAt().toString()
        );
    }

    private String generateSha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to generate event hash.", exception);
        }
    }

    private String generateMockTransactionHash(String eventHash) {
        return "0x" + eventHash.substring(0, 32) + eventHash.substring(32, 64);
    }

    private Long generateMockBlockNumber() {
        return ThreadLocalRandom.current().nextLong(100000L, 999999L);
    }

    private BlockchainRecordResponse mapToResponse(BlockchainRecord blockchainRecord) {
        return new BlockchainRecordResponse(
                blockchainRecord.getId(),
                blockchainRecord.getShipmentId(),
                blockchainRecord.getShipmentEventId(),
                blockchainRecord.getEventHash(),
                blockchainRecord.getTransactionHash(),
                blockchainRecord.getBlockNumber(),
                blockchainRecord.getStatus(),
                blockchainRecord.getRecordedAt(),
                blockchainRecord.getVerifiedAt(),
                blockchainRecord.getVerificationMessage()
        );
    }
}