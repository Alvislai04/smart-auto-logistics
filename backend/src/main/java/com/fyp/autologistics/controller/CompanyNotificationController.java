package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.CompanyNotificationResponse;
import com.fyp.autologistics.service.CompanyNotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/company-notifications")
public class CompanyNotificationController {

    private final CompanyNotificationService companyNotificationService;

    public CompanyNotificationController(CompanyNotificationService companyNotificationService) {
        this.companyNotificationService = companyNotificationService;
    }

    @GetMapping
    public List<CompanyNotificationResponse> getAllCompanyNotifications() {
        return companyNotificationService.getAllCompanyNotifications();
    }
}
