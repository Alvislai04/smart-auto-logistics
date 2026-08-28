package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.BlockchainRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecord, Long> {

    List<BlockchainRecord> findAllByOrderByRecordedAtDesc();

    List<BlockchainRecord> findByShipmentIdOrderByRecordedAtDesc(Long shipmentId);

    Optional<BlockchainRecord> findByShipmentEventId(Long shipmentEventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select record from BlockchainRecord record where record.id = :recordId")
    Optional<BlockchainRecord> findByIdForUpdate(@Param("recordId") Long recordId);
}
