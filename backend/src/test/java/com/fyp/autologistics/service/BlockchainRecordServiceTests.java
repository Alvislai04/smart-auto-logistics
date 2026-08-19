package com.fyp.autologistics.service;

import com.fyp.autologistics.blockchain.BlockchainHealthSnapshot;
import com.fyp.autologistics.blockchain.BlockchainLedgerClient;
import com.fyp.autologistics.blockchain.BlockchainLedgerException;
import com.fyp.autologistics.blockchain.BlockchainTransactionReceipt;
import com.fyp.autologistics.dto.BlockchainHealthResponse;
import com.fyp.autologistics.dto.BlockchainRecordResponse;
import com.fyp.autologistics.entity.BlockchainRecord;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.enums.BlockchainHealthStatus;
import com.fyp.autologistics.enums.BlockchainRecordStatus;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.repository.BlockchainRecordRepository;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlockchainRecordServiceTests {

    private final BlockchainRecordRepository blockchainRecordRepository =
            mock(BlockchainRecordRepository.class);
    private final ShipmentEventRepository shipmentEventRepository =
            mock(ShipmentEventRepository.class);
    private final AdminNotificationService adminNotificationService =
            mock(AdminNotificationService.class);
    private final CompanyNotificationService companyNotificationService =
            mock(CompanyNotificationService.class);
    private final IntegrityCaseService integrityCaseService = mock(IntegrityCaseService.class);
    private final BlockchainLedgerClient blockchainLedgerClient = mock(BlockchainLedgerClient.class);

    private final BlockchainRecordService blockchainRecordService = new BlockchainRecordService(
            blockchainRecordRepository,
            shipmentEventRepository,
            adminNotificationService,
            companyNotificationService,
            integrityCaseService,
            blockchainLedgerClient
    );

    @Test
    void failedWriteCanBeRetriedAndKeepsItsDatabaseRecord() {
        ShipmentEvent shipmentEvent = buildShipmentEvent();
        AtomicReference<BlockchainRecord> savedRecordReference = new AtomicReference<>();

        when(blockchainRecordRepository.findByShipmentEventId(101L))
                .thenReturn(Optional.empty());
        when(blockchainRecordRepository.save(any(BlockchainRecord.class)))
                .thenAnswer(invocation -> {
                    BlockchainRecord record = invocation.getArgument(0);
                    ReflectionTestUtils.setField(record, "id", 88L);
                    savedRecordReference.set(record);
                    return record;
                });
        when(blockchainLedgerClient.recordShipmentEvent(any(), any(), any()))
                .thenThrow(new BlockchainLedgerException("Node unavailable."))
                .thenReturn(new BlockchainTransactionReceipt("0xabc123", 42L));

        BlockchainRecordResponse initialResponse =
                blockchainRecordService.recordShipmentEventAutomatically(shipmentEvent);
        BlockchainRecord failedRecord = savedRecordReference.get();

        assertThat(initialResponse.getStatus()).isEqualTo(BlockchainRecordStatus.FAILED);
        when(blockchainRecordRepository.findByIdForUpdate(88L))
                .thenReturn(Optional.of(failedRecord));
        when(shipmentEventRepository.findById(101L)).thenReturn(Optional.of(shipmentEvent));

        BlockchainRecordResponse retriedResponse =
                blockchainRecordService.retryFailedRecord(88L);

        assertThat(retriedResponse.getId()).isEqualTo(88L);
        assertThat(retriedResponse.getStatus()).isEqualTo(BlockchainRecordStatus.RECORDED);
        assertThat(retriedResponse.getTransactionHash()).isEqualTo("0xabc123");
        assertThat(retriedResponse.getBlockNumber()).isEqualTo(42L);
        assertThat(retriedResponse.getRetryCount()).isEqualTo(1);
        assertThat(retriedResponse.getLastRetryAt()).isNotNull();
        verify(adminNotificationService).markUnreadReferenceNotificationsAsRead(
                "BLOCKCHAIN_RECORD",
                88L
        );
    }

    @Test
    void failedVerificationRecordCannotBeWrittenAgain() {
        BlockchainRecord record = new BlockchainRecord();
        record.setStatus(BlockchainRecordStatus.FAILED);
        record.setTransactionHash("0xalready-recorded");
        record.setBlockNumber(12L);

        when(blockchainRecordRepository.findByIdForUpdate(91L))
                .thenReturn(Optional.of(record));

        assertThatThrownBy(() -> blockchainRecordService.retryFailedRecord(91L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already written on-chain");

        verify(blockchainLedgerClient, never()).recordShipmentEvent(any(), any(), any());
        verify(blockchainRecordRepository, never()).save(any(BlockchainRecord.class));
    }

    @Test
    void retryAlwaysUsesTheOriginalStoredHash() {
        ShipmentEvent shipmentEvent = buildShipmentEvent();
        BlockchainRecord record = new BlockchainRecord();
        ReflectionTestUtils.setField(record, "id", 92L);
        record.setShipmentId(7L);
        record.setShipmentEventId(101L);
        record.setEventHash("original-stored-hash");
        record.setStatus(BlockchainRecordStatus.FAILED);

        when(blockchainRecordRepository.findByIdForUpdate(92L))
                .thenReturn(Optional.of(record));
        when(shipmentEventRepository.findById(101L)).thenReturn(Optional.of(shipmentEvent));
        when(blockchainLedgerClient.recordShipmentEvent(any(), any(), any()))
                .thenThrow(new BlockchainLedgerException("Integration disabled."));
        when(blockchainRecordRepository.save(record)).thenReturn(record);

        BlockchainRecordResponse response = blockchainRecordService.retryFailedRecord(92L);

        assertThat(response.getStatus()).isEqualTo(BlockchainRecordStatus.FAILED);
        assertThat(response.getEventHash()).isEqualTo("original-stored-hash");
        assertThat(response.getRetryCount()).isEqualTo(1);
        verify(blockchainLedgerClient).recordShipmentEvent(
                7L,
                101L,
                "original-stored-hash"
        );
    }

    @Test
    void healthResponseMapsLiveLedgerSnapshotWithoutSignerSecrets() {
        BlockchainHealthSnapshot snapshot = new BlockchainHealthSnapshot(
                BlockchainHealthStatus.HEALTHY,
                true,
                true,
                true,
                true,
                true,
                "0xcontract",
                31337L,
                120L,
                "Blockchain services are ready."
        );
        when(blockchainLedgerClient.getHealthSnapshot()).thenReturn(snapshot);

        BlockchainHealthResponse response = blockchainRecordService.getHealth();

        assertThat(response.status()).isEqualTo(BlockchainHealthStatus.HEALTHY);
        assertThat(response.connected()).isTrue();
        assertThat(response.contractAddress()).isEqualTo("0xcontract");
        assertThat(response.chainId()).isEqualTo(31337L);
        assertThat(response.latestBlockNumber()).isEqualTo(120L);
        assertThat(response.checkedAt()).isNotNull();
    }

    private ShipmentEvent buildShipmentEvent() {
        Shipment shipment = new Shipment();
        ReflectionTestUtils.setField(shipment, "id", 7L);
        shipment.setShipmentCode("SHP-TEST-001");

        ShipmentEvent shipmentEvent = new ShipmentEvent();
        ReflectionTestUtils.setField(shipmentEvent, "id", 101L);
        ReflectionTestUtils.setField(
                shipmentEvent,
                "createdAt",
                LocalDateTime.of(2026, 8, 13, 10, 15)
        );
        shipmentEvent.setShipment(shipment);
        shipmentEvent.setEventType(ShipmentEventType.SHIPMENT_CREATED);
        shipmentEvent.setDescription("Shipment created for blockchain retry test.");
        return shipmentEvent;
    }
}
