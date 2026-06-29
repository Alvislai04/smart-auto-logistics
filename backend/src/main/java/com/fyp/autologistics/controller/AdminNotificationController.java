package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.AdminNotificationResponse;
import com.fyp.autologistics.service.AdminNotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    public AdminNotificationController(AdminNotificationService adminNotificationService) {
        this.adminNotificationService = adminNotificationService;
    }

    @GetMapping
    public List<AdminNotificationResponse> getAllNotifications() {
        return adminNotificationService.getAllNotifications();
    }

    @GetMapping("/unread")
    public List<AdminNotificationResponse> getUnreadNotifications() {
        return adminNotificationService.getUnreadNotifications();
    }

    @PutMapping("/{notificationId}/read")
    public AdminNotificationResponse markAsRead(@PathVariable Long notificationId) {
        return adminNotificationService.markAsRead(notificationId);
    }
}