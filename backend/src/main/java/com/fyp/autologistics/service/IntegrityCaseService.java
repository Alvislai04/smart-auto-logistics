package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.IntegrityCaseResponse;
import com.fyp.autologistics.dto.ResolveIntegrityCaseRequest;
import com.fyp.autologistics.dto.UpdateIntegrityCaseStatusRequest;
import com.fyp.autologistics.entity.BlockchainRecord;
import com.fyp.autologistics.entity.IntegrityCase;
import com.fyp.autologistics.enums.IntegrityCaseStatus;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.IntegrityCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IntegrityCaseService {

    private final IntegrityCaseRepository integrityCaseRepository;

    public IntegrityCaseService(IntegrityCaseRepository integrityCaseRepository) {
        this.integrityCaseRepository = integrityCaseRepository;
    }

    @Transactional
    public IntegrityCaseResponse createCaseIfNotExists(BlockchainRecord blockchainRecord) {
        return integrityCaseRepository.findByBlockchainRecordId(blockchainRecord.getId())
                .map(this::mapToResponse)
                .orElseGet(() -> createNewCase(blockchainRecord));
    }

    @Transactional(readOnly = true)
    public List<IntegrityCaseResponse> getAllCases() {
        return integrityCaseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IntegrityCaseResponse> getCasesByStatus(IntegrityCaseStatus status) {
        return integrityCaseRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IntegrityCaseResponse getCaseById(Long caseId) {
        IntegrityCase integrityCase = findCaseById(caseId);
        return mapToResponse(integrityCase);
    }

    @Transactional
    public IntegrityCaseResponse updateCaseStatus(
            Long caseId,
            UpdateIntegrityCaseStatusRequest request
    ) {
        IntegrityCase integrityCase = findCaseById(caseId);

        if (integrityCase.getStatus() == IntegrityCaseStatus.RESOLVED) {
            throw new BadRequestException("Resolved integrity case cannot be updated.");
        }

        integrityCase.setStatus(request.getStatus());

        if (request.getInvestigationNotes() != null && !request.getInvestigationNotes().isBlank()) {
            integrityCase.setInvestigationNotes(request.getInvestigationNotes());
        }

        IntegrityCase savedCase = integrityCaseRepository.save(integrityCase);
        return mapToResponse(savedCase);
    }

    @Transactional
    public IntegrityCaseResponse resolveCase(
            Long caseId,
            ResolveIntegrityCaseRequest request
    ) {
        IntegrityCase integrityCase = findCaseById(caseId);

        if (integrityCase.getStatus() == IntegrityCaseStatus.RESOLVED) {
            throw new BadRequestException("Integrity case is already resolved.");
        }

        integrityCase.setStatus(IntegrityCaseStatus.RESOLVED);
        integrityCase.setResolutionType(request.getResolutionType());
        integrityCase.setResolutionNotes(request.getResolutionNotes());
        integrityCase.setResolvedAt(LocalDateTime.now());

        IntegrityCase savedCase = integrityCaseRepository.save(integrityCase);
        return mapToResponse(savedCase);
    }

    private IntegrityCaseResponse createNewCase(BlockchainRecord blockchainRecord) {
        IntegrityCase integrityCase = new IntegrityCase();
        integrityCase.setBlockchainRecordId(blockchainRecord.getId());
        integrityCase.setShipmentId(blockchainRecord.getShipmentId());
        integrityCase.setShipmentEventId(blockchainRecord.getShipmentEventId());
        integrityCase.setStatus(IntegrityCaseStatus.OPEN);
        integrityCase.setTitle("Blockchain integrity mismatch detected");
        integrityCase.setDescription(
                "Blockchain verification failed for record #"
                        + blockchainRecord.getId()
                        + ". Shipment #"
                        + blockchainRecord.getShipmentId()
                        + ", shipment event #"
                        + blockchainRecord.getShipmentEventId()
                        + " requires investigation."
        );

        IntegrityCase savedCase = integrityCaseRepository.save(integrityCase);
        return mapToResponse(savedCase);
    }

    private IntegrityCase findCaseById(Long caseId) {
        return integrityCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Integrity case not found with id: " + caseId
                ));
    }

    private IntegrityCaseResponse mapToResponse(IntegrityCase integrityCase) {
        return new IntegrityCaseResponse(
                integrityCase.getId(),
                integrityCase.getBlockchainRecordId(),
                integrityCase.getShipmentId(),
                integrityCase.getShipmentEventId(),
                integrityCase.getStatus(),
                integrityCase.getResolutionType(),
                integrityCase.getTitle(),
                integrityCase.getDescription(),
                integrityCase.getInvestigationNotes(),
                integrityCase.getResolutionNotes(),
                integrityCase.getCreatedAt(),
                integrityCase.getUpdatedAt(),
                integrityCase.getResolvedAt()
        );
    }
}