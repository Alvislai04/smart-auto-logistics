package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.IntegrityCaseResponse;
import com.fyp.autologistics.dto.ResolveIntegrityCaseRequest;
import com.fyp.autologistics.dto.UpdateIntegrityCaseStatusRequest;
import com.fyp.autologistics.enums.IntegrityCaseStatus;
import com.fyp.autologistics.service.IntegrityCaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/integrity-cases")
@CrossOrigin(origins = "http://localhost:5173")
public class IntegrityCaseController {

    private final IntegrityCaseService integrityCaseService;

    public IntegrityCaseController(IntegrityCaseService integrityCaseService) {
        this.integrityCaseService = integrityCaseService;
    }

    @GetMapping
    public List<IntegrityCaseResponse> getCases(
            @RequestParam(required = false) IntegrityCaseStatus status
    ) {
        if (status == null) {
            return integrityCaseService.getAllCases();
        }

        return integrityCaseService.getCasesByStatus(status);
    }

    @GetMapping("/{caseId}")
    public IntegrityCaseResponse getCaseById(@PathVariable Long caseId) {
        return integrityCaseService.getCaseById(caseId);
    }

    @PutMapping("/{caseId}/status")
    public IntegrityCaseResponse updateCaseStatus(
            @PathVariable Long caseId,
            @Valid @RequestBody UpdateIntegrityCaseStatusRequest request
    ) {
        return integrityCaseService.updateCaseStatus(caseId, request);
    }

    @PutMapping("/{caseId}/resolve")
    public IntegrityCaseResponse resolveCase(
            @PathVariable Long caseId,
            @Valid @RequestBody ResolveIntegrityCaseRequest request
    ) {
        return integrityCaseService.resolveCase(caseId, request);
    }
}