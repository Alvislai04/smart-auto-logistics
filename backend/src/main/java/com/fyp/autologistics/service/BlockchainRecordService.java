package com.fyp.autologistics.service;

import com.fyp.autologistics.blockchain.BlockchainHealthSnapshot;
import com.fyp.autologistics.blockchain.BlockchainLedgerClient;
import com.fyp.autologistics.blockchain.BlockchainLedgerException;
import com.fyp.autologistics.blockchain.BlockchainTransactionReceipt;
import com.fyp.autologistics.config.BlockchainProperties;
import com.fyp.autologistics.dto.BlockchainHealthResponse;
import com.fyp.autologistics.dto.BlockchainRecordResponse;
import com.fyp.autologistics.entity.BlockchainRecord;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.enums.AdminNotificationType;
import com.fyp.autologistics.enums.BlockchainRecordStatus;
import com.fyp.autologistics.enums.CompanyNotificationType;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.BlockchainRecordRepository;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class BlockchainRecordService {

    private final BlockchainRecordRepository blockchainRecordRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final AdminNotificationService adminNotificationService;
    private final CompanyNotificationService companyNotificationService;
    private final IntegrityCaseService integrityCaseService;
    private final BlockchainProperties blockchainProperties;
    private final BlockchainLedgerClient blockchainLedgerClient;

    public BlockchainRecordService(
        BlockchainRecordRepository blockchainRecordRepository,
        ShipmentEventRepository shipmentEventRepository,
        AdminNotificationService adminNotificationService,
        CompanyNotificationService companyNotificationService,
        IntegrityCaseService integrityCaseService,
        BlockchainProperties blockchainProperties,
        BlockchainLedgerClient blockchainLedgerClient
    ) {
        this.blockchainRecordRepository = blockchainRecordRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.adminNotificationService = adminNotificationService;
        this.companyNotificationService = companyNotificationService;
        this.integrityCaseService = integrityCaseService;
        this.blockchainProperties = blockchainProperties;
        this.blockchainLedgerClient = blockchainLedgerClient;
    }

    @Transactional
    public Optional<BlockchainRecordResponse> recordShipmentEventAutomatically(
            ShipmentEvent shipmentEvent
    ) {
        if (!blockchainProperties.isEnabled()) {
            return Optional.empty();
        }

        Optional<BlockchainRecord> existingRecord =
                blockchainRecordRepository.findByShipmentEventId(shipmentEvent.getId());

        if (existingRecord.isPresent()) {
            return Optional.of(mapToResponse(existingRecord.get()));
        }

        String eventPayload = buildEventPayload(shipmentEvent);
        String eventHash = generateSha256Hash(eventPayload);

        BlockchainRecord savedRecord = saveRecordWithLedgerResult(
                shipmentEvent.getShipment().getId(),
                shipmentEvent.getId(),
                eventHash
        );

        return Optional.of(mapToResponse(savedRecord));
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

    public BlockchainHealthResponse getHealth() {
        BlockchainHealthSnapshot snapshot = blockchainLedgerClient.getHealthSnapshot();
        return new BlockchainHealthResponse(
                snapshot.status(),
                snapshot.enabled(),
                snapshot.connected(),
                snapshot.contractConfigured(),
                snapshot.contractDeployed(),
                snapshot.signerConfigured(),
                snapshot.contractAddress(),
                snapshot.chainId(),
                snapshot.latestBlockNumber(),
                snapshot.message(),
                LocalDateTime.now()
        );
    }

    @Transactional
    public BlockchainRecordResponse retryFailedRecord(Long blockchainRecordId) {
        BlockchainRecord blockchainRecord = blockchainRecordRepository
                .findByIdForUpdate(blockchainRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blockchain record not found with id: " + blockchainRecordId
                ));

        validateRecordCanBeRetried(blockchainRecord);

        ShipmentEvent shipmentEvent = shipmentEventRepository.findById(
                blockchainRecord.getShipmentEventId()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Shipment event not found with id: " + blockchainRecord.getShipmentEventId()
        ));

        blockchainRecord.setRetryCount(
                (blockchainRecord.getRetryCount() == null ? 0 : blockchainRecord.getRetryCount()) + 1
        );
        blockchainRecord.setLastRetryAt(LocalDateTime.now());

        try {
            BlockchainTransactionReceipt receipt = blockchainLedgerClient.recordShipmentEvent(
                    blockchainRecord.getShipmentId(),
                    blockchainRecord.getShipmentEventId(),
                    blockchainRecord.getEventHash()
            );

            blockchainRecord.setTransactionHash(receipt.transactionHash());
            blockchainRecord.setBlockNumber(receipt.blockNumber());
            blockchainRecord.setStatus(BlockchainRecordStatus.RECORDED);
            blockchainRecord.setVerificationMessage(
                    "Blockchain recording retry succeeded. The original stored shipment event "
                            + "hash is now on-chain."
            );
        } catch (BlockchainLedgerException exception) {
            blockchainRecord.setStatus(BlockchainRecordStatus.FAILED);
            blockchainRecord.setVerificationMessage(
                    "Blockchain recording retry failed: " + exception.getMessage()
            );
        }

        BlockchainRecord savedRecord = blockchainRecordRepository.save(blockchainRecord);

        if (savedRecord.getStatus() == BlockchainRecordStatus.RECORDED) {
            adminNotificationService.markUnreadReferenceNotificationsAsRead(
                    "BLOCKCHAIN_RECORD",
                    savedRecord.getId()
            );
        } else {
            createBlockchainFailureNotification(savedRecord);
        }

        return mapToResponse(savedRecord);
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

        blockchainRecord.setVerifiedAt(LocalDateTime.now());

        String onChainHash;
        try {
            onChainHash = blockchainLedgerClient.getRecordedEventHash(
                    blockchainRecord.getShipmentEventId()
            );
        } catch (BlockchainLedgerException exception) {
            blockchainRecord.setStatus(BlockchainRecordStatus.FAILED);
            blockchainRecord.setVerificationMessage(
                    "Unable to verify against blockchain ledger: " + exception.getMessage()
            );

            BlockchainRecord savedRecord = blockchainRecordRepository.save(blockchainRecord);
            createBlockchainFailureNotification(savedRecord);
            return mapToResponse(savedRecord);
        }

        boolean localHashMatchesEvent = hashesMatch(blockchainRecord.getEventHash(), recalculatedHash);
        boolean onChainHashMatchesRecord = hashesMatch(blockchainRecord.getEventHash(), onChainHash);

        if (localHashMatchesEvent && onChainHashMatchesRecord) {
            blockchainRecord.setStatus(BlockchainRecordStatus.RECORDED);
            blockchainRecord.setVerificationMessage(
                    "Blockchain record verified successfully. Local event hash matches the "
                            + "on-chain ledger hash."
            );
        } else {
            blockchainRecord.setStatus(BlockchainRecordStatus.FAILED);
            blockchainRecord.setVerificationMessage(buildVerificationFailureMessage(
                    localHashMatchesEvent,
                    onChainHashMatchesRecord,
                    onChainHash
            ));

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
        String basePayload = String.join(
                "|",
                shipmentEvent.getShipment().getId().toString(),
                shipmentEvent.getShipment().getShipmentCode(),
                shipmentEvent.getId().toString(),
                shipmentEvent.getEventType().name(),
                shipmentEvent.getDescription(),
                shipmentEvent.getPreviousStatus() == null ? "" : shipmentEvent.getPreviousStatus(),
                shipmentEvent.getNewStatus() == null ? "" : shipmentEvent.getNewStatus(),
                formatEventTimestamp(shipmentEvent.getCreatedAt())
        );

        if (!usesVersionedAuditPayload(shipmentEvent)) {
            return basePayload;
        }

        return String.join(
                "|",
                basePayload,
                nullablePayloadValue(shipmentEvent.getActorUserId()),
                nullablePayloadValue(shipmentEvent.getActorName()),
                nullablePayloadValue(shipmentEvent.getActorEmail()),
                nullablePayloadValue(shipmentEvent.getActorRole()),
                nullablePayloadValue(shipmentEvent.getSourceType()),
                nullablePayloadValue(shipmentEvent.getEventPayloadVersion())
        );
    }

    private boolean usesVersionedAuditPayload(ShipmentEvent shipmentEvent) {
        return shipmentEvent.getEventPayloadVersion() != null
                && shipmentEvent.getEventPayloadVersion() >= 2;
    }

    private String nullablePayloadValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String formatEventTimestamp(LocalDateTime createdAt) {
        return createdAt.truncatedTo(ChronoUnit.MICROS).toString();
    }

    private BlockchainRecord saveRecordWithLedgerResult(
            Long shipmentId,
            Long shipmentEventId,
            String eventHash
    ) {
        BlockchainRecord blockchainRecord = new BlockchainRecord();
        blockchainRecord.setShipmentId(shipmentId);
        blockchainRecord.setShipmentEventId(shipmentEventId);
        blockchainRecord.setEventHash(eventHash);
        blockchainRecord.setStatus(BlockchainRecordStatus.PENDING);

        try {
            BlockchainTransactionReceipt receipt = blockchainLedgerClient.recordShipmentEvent(
                    shipmentId,
                    shipmentEventId,
                    eventHash
            );

            blockchainRecord.setTransactionHash(receipt.transactionHash());
            blockchainRecord.setBlockNumber(receipt.blockNumber());
            blockchainRecord.setStatus(BlockchainRecordStatus.RECORDED);
            blockchainRecord.setVerificationMessage("Shipment event hash recorded on blockchain.");
        } catch (BlockchainLedgerException exception) {
            blockchainRecord.setStatus(BlockchainRecordStatus.FAILED);
            blockchainRecord.setVerificationMessage(
                    "Blockchain recording failed: " + exception.getMessage()
            );
        }

        BlockchainRecord savedRecord = blockchainRecordRepository.save(blockchainRecord);

        if (savedRecord.getStatus() == BlockchainRecordStatus.FAILED) {
            createBlockchainFailureNotification(savedRecord);
        }

        return savedRecord;
    }

    private void validateRecordCanBeRetried(BlockchainRecord blockchainRecord) {
        if (blockchainRecord.getStatus() != BlockchainRecordStatus.FAILED) {
            throw new BadRequestException("Only failed blockchain records can be retried.");
        }

        if (StringUtils.hasText(blockchainRecord.getTransactionHash())
                || blockchainRecord.getBlockNumber() != null) {
            throw new BadRequestException(
                    "This failed record was already written on-chain and must be verified or "
                            + "investigated instead of retried."
            );
        }
    }

    private void createBlockchainFailureNotification(BlockchainRecord blockchainRecord) {
        adminNotificationService.createUnreadNotificationIfNotExists(
                AdminNotificationType.SYSTEM_ALERT,
                "Blockchain ledger action failed",
                "Blockchain record #"
                        + blockchainRecord.getId()
                        + " for shipment #"
                        + blockchainRecord.getShipmentId()
                        + " and shipment event #"
                        + blockchainRecord.getShipmentEventId()
                        + " needs attention. "
                        + blockchainRecord.getVerificationMessage(),
                "BLOCKCHAIN_RECORD",
                blockchainRecord.getId()
        );
    }

    private boolean hashesMatch(String leftHash, String rightHash) {
        return normalizeHash(leftHash).equals(normalizeHash(rightHash));
    }

    private String normalizeHash(String hash) {
        if (hash == null) {
            return "";
        }

        return hash.startsWith("0x")
                ? hash.substring(2).toLowerCase()
                : hash.toLowerCase();
    }

    private String buildVerificationFailureMessage(
            boolean localHashMatchesEvent,
            boolean onChainHashMatchesRecord,
            String onChainHash
    ) {
        if (!localHashMatchesEvent && !onChainHashMatchesRecord) {
            return "Hash mismatch detected. The shipment event data changed locally and the "
                    + "database hash does not match the on-chain ledger hash. On-chain hash: "
                    + onChainHash;
        }

        if (!localHashMatchesEvent) {
            return "Local shipment event data no longer matches the stored event hash. "
                    + "The shipment event may have been modified after recording.";
        }

        return "Stored event hash does not match the on-chain ledger hash. On-chain hash: "
                + onChainHash;
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
                blockchainRecord.getVerificationMessage(),
                blockchainRecord.getRetryCount(),
                blockchainRecord.getLastRetryAt()
        );
    }
}
