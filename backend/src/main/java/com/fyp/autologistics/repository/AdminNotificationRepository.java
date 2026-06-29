package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.AdminNotification;
import com.fyp.autologistics.enums.AdminNotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    List<AdminNotification> findAllByOrderByCreatedAtDesc();

    List<AdminNotification> findByStatusOrderByCreatedAtDesc(AdminNotificationStatus status);

    boolean existsByReferenceTypeAndReferenceIdAndStatus(
            String referenceType,
            Long referenceId,
            AdminNotificationStatus status
    );
}