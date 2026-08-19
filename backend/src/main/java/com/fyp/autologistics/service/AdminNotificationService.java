package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.AdminNotificationResponse;
import com.fyp.autologistics.entity.AdminNotification;
import com.fyp.autologistics.enums.AdminNotificationStatus;
import com.fyp.autologistics.enums.AdminNotificationType;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.AdminNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;

    public AdminNotificationService(AdminNotificationRepository adminNotificationRepository) {
        this.adminNotificationRepository = adminNotificationRepository;
    }

    @Transactional
    public AdminNotificationResponse createUnreadNotificationIfNotExists(
            AdminNotificationType type,
            String title,
            String message,
            String referenceType,
            Long referenceId
    ) {
        boolean duplicateUnreadNotificationExists =
                adminNotificationRepository.existsByReferenceTypeAndReferenceIdAndStatus(
                        referenceType,
                        referenceId,
                        AdminNotificationStatus.UNREAD
                );

        if (duplicateUnreadNotificationExists) {
            return getLatestNotificationByReference(referenceType, referenceId);
        }

        AdminNotification notification = new AdminNotification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setStatus(AdminNotificationStatus.UNREAD);

        AdminNotification savedNotification = adminNotificationRepository.save(notification);
        return mapToResponse(savedNotification);
    }

    @Transactional(readOnly = true)
    public List<AdminNotificationResponse> getAllNotifications() {
        return adminNotificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminNotificationResponse> getUnreadNotifications() {
        return adminNotificationRepository.findByStatusOrderByCreatedAtDesc(AdminNotificationStatus.UNREAD)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public AdminNotificationResponse markAsRead(Long notificationId) {
        AdminNotification notification = adminNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admin notification not found with id: " + notificationId
                ));

        notification.setStatus(AdminNotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        AdminNotification savedNotification = adminNotificationRepository.save(notification);
        return mapToResponse(savedNotification);
    }

    @Transactional
    public void markUnreadReferenceNotificationsAsRead(
            String referenceType,
            Long referenceId
    ) {
        List<AdminNotification> notifications =
                adminNotificationRepository.findByReferenceTypeAndReferenceIdAndStatus(
                        referenceType,
                        referenceId,
                        AdminNotificationStatus.UNREAD
                );

        LocalDateTime readAt = LocalDateTime.now();
        notifications.forEach(notification -> {
            notification.setStatus(AdminNotificationStatus.READ);
            notification.setReadAt(readAt);
        });

        adminNotificationRepository.saveAll(notifications);
    }

    private AdminNotificationResponse getLatestNotificationByReference(
            String referenceType,
            Long referenceId
    ) {
        return adminNotificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(notification -> referenceType.equals(notification.getReferenceType()))
                .filter(notification -> referenceId.equals(notification.getReferenceId()))
                .findFirst()
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Admin notification not found"));
    }

    private AdminNotificationResponse mapToResponse(AdminNotification notification) {
        return new AdminNotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
