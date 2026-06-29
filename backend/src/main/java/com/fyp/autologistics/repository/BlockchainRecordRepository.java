package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.BlockchainRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecord, Long> {

    List<BlockchainRecord> findAllByOrderByRecordedAtDesc();

    List<BlockchainRecord> findByShipmentIdOrderByRecordedAtDesc(Long shipmentId);

    Optional<BlockchainRecord> findByShipmentEventId(Long shipmentEventId);

    boolean existsByShipmentEventId(Long shipmentEventId);
}