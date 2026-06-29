package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.CompanyNotificationResponse;
import com.fyp.autologistics.entity.CompanyNotification;
import com.fyp.autologistics.enums.CompanyNotificationStatus;
import com.fyp.autologistics.enums.CompanyNotificationType;
import com.fyp.autologistics.repository.CompanyNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyNotificationService {

    private final CompanyNotificationRepository companyNotificationRepository;

    public CompanyNotificationService(CompanyNotificationRepository companyNotificationRepository) {
        this.companyNotificationRepository = companyNotificationRepository;
    }

    @Transactional
    public CompanyNotificationResponse createCompanyNotificationIfNotExists(
            CompanyNotificationType type,
            String recipientCompany,
            String title,
            String message,
            String referenceType,
            Long referenceId
    ) {
        boolean alreadyExists = companyNotificationRepository.existsByReferenceTypeAndReferenceId(
                referenceType,
                referenceId
        );

        if (alreadyExists) {
            return companyNotificationRepository.findAllByOrderBySentAtDesc()
                    .stream()
                    .filter(notification -> referenceType.equals(notification.getReferenceType()))
                    .filter(notification -> referenceId.equals(notification.getReferenceId()))
                    .findFirst()
                    .map(this::mapToResponse)
                    .orElseThrow();
        }

        CompanyNotification notification = new CompanyNotification();
        notification.setType(type);
        notification.setRecipientCompany(recipientCompany);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setStatus(CompanyNotificationStatus.SENT);

        CompanyNotification savedNotification = companyNotificationRepository.save(notification);
        return mapToResponse(savedNotification);
    }

    @Transactional(readOnly = true)
    public List<CompanyNotificationResponse> getAllCompanyNotifications() {
        return companyNotificationRepository.findAllByOrderBySentAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CompanyNotificationResponse mapToResponse(CompanyNotification notification) {
        return new CompanyNotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getStatus(),
                notification.getRecipientCompany(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getSentAt()
        );
    }
}