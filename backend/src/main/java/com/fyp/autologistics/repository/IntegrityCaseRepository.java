package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.IntegrityCase;
import com.fyp.autologistics.enums.IntegrityCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntegrityCaseRepository extends JpaRepository<IntegrityCase, Long> {

    List<IntegrityCase> findAllByOrderByCreatedAtDesc();

    List<IntegrityCase> findByStatusOrderByCreatedAtDesc(IntegrityCaseStatus status);

    Optional<IntegrityCase> findByBlockchainRecordId(Long blockchainRecordId);

    boolean existsByBlockchainRecordId(Long blockchainRecordId);
}